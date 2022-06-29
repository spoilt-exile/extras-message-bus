/*
 * This file is part of MessageBus library.
 * 
 * Copyright (C) 2020 Freax Software
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package tk.freaxsoftware.extras.bus.executor.impl;

import tk.freaxsoftware.extras.bus.GlobalCons;
import tk.freaxsoftware.extras.bus.MessageBusInit;
import tk.freaxsoftware.extras.bus.MessageContext;
import tk.freaxsoftware.extras.bus.MessageContextHolder;
import tk.freaxsoftware.extras.bus.MessageHolder;
import tk.freaxsoftware.extras.bus.MessageStatus;
import tk.freaxsoftware.extras.bus.Receiver;
import tk.freaxsoftware.extras.bus.Subscription;
import tk.freaxsoftware.extras.bus.exceptions.ExceptionServices;
import tk.freaxsoftware.extras.bus.exceptions.NoSubscriptionMessageException;
import tk.freaxsoftware.extras.bus.executor.MessageExecutor;

/**
 * Default executor for CALL delivery.
 * @author Stanislav Nepochatov
 * @see MessageOptions.DeliveryPolicy#CALL
 * @since 5.0
 */
public class CallMessageExecutor extends MessageExecutor {

    public CallMessageExecutor(MessageHolder holder, Subscription subscription, MessageBusInit init) {
        super(holder, subscription, init);
    }

    @Override
    public void exec() {
        MessageContextHolder.setContext(new MessageContext(holder.getTrxId()));
        init.getInterceptor().storeMessage(holder);
        if (subscription != null) {
            Integer tryIndex = 0;
            while (tryIndex < holder.getOptions().getRedeliveryCounter()) {
                Receiver rc = subscription.getRoundRobinIterator().next();
                try {
                    rc.receive(holder);
                } catch (Exception ex) {
                    LOGGER.error("Receiver " + rc.getClass().getName() + " for topic " + holder.getTopic() + " throws exception", ex);
                    holder.getResponse().getHeaders().put(GlobalCons.G_EXCEPTION_HEADER, ex.getClass().getCanonicalName());
                    holder.getResponse().getHeaders().put(GlobalCons.G_EXCEPTION_MESSAGE_HEADER, ex.getMessage());
                    ExceptionServices.handle(holder.getResponse(), ex);
                    if (holder.getStatus() != MessageStatus.FINISHED) {
                        holder.setStatus(MessageStatus.ERROR);
                        init.getInterceptor().storeMessage(holder);
                    }
                    tryIndex++;
                }
                if (holder.getOptions().getCallback() != null) {
                    holder.setStatus(MessageStatus.CALLBACK);
                    holder.getOptions().getCallback().callback(holder.getResponse());
                }
                if (holder.getStatus() != MessageStatus.REMOTE_PROCESSING) {
                    holder.setStatus(MessageStatus.FINISHED);
                }
                init.getInterceptor().storeProcessedMessage(holder);
                break;
            }
            if (holder.getStatus() != MessageStatus.FINISHED) {
                LOGGER.warn("Message {} on topic {} exhaust redelivery attempts, dropping.", 
                        holder.getId(), holder.getTopic());
            }
        } else {
            holder.setStatus(MessageStatus.ERROR);
            init.getInterceptor().storeMessage(holder);
            throw new NoSubscriptionMessageException(String.format("No subscribers for message %s", holder.getTopic()));
        }
        

    }
    
}
