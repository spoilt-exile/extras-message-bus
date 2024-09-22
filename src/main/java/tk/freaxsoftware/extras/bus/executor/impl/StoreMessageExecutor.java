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
import tk.freaxsoftware.extras.bus.Subscription;
import tk.freaxsoftware.extras.bus.exceptions.ExceptionServices;
import tk.freaxsoftware.extras.bus.executor.MessageExecutor;

/**
 * Default executor for STORE delivery.
 * @author Stanislav Nepochatov
 * @see MessageOptions.DeliveryPolicy#STORE
 * @since 5.0
 */
public class StoreMessageExecutor extends MessageExecutor {

    public StoreMessageExecutor(MessageHolder holder, Subscription subscription, MessageBusInit init) {
        super(holder, subscription, init);
    }

    @Override
    public void exec() {
        if (isMessagePresent(holder.getId()) && !holder.getHeaders().containsKey(GlobalCons.G_REDELIVERY_MODE_HEADER)) {
            LOGGER.info("Message {} already present in storage, skipping;", holder.getId());
            return;
        }
        MessageContextHolder.setContext(new MessageContext(holder.getTrxId()));
        init.getInterceptor().storeMessage(holder);
        if (subscription != null) {
            subscription.getReceiversByMode(holder.getOptions().isBroadcast()).forEach(rc -> {
                try {
                    rc.receive(holder);
                    if (holder.getStatus() != MessageStatus.GROUPING && 
                            holder.getStatus() != MessageStatus.REMOTE_PROCESSING) {
                        holder.setStatus(MessageStatus.FINISHED);
                    }
                    init.getInterceptor().storeProcessedMessage(holder);
                } catch (Exception ex) {
                    LOGGER.error("Receiver " + rc.getClass().getName() + " for topic " + holder.getTopic() + " throws exception", ex);
                    holder.getResponse().getHeaders().put(GlobalCons.G_EXCEPTION_HEADER, ex.getClass().getCanonicalName());
                    holder.getResponse().getHeaders().put(GlobalCons.G_EXCEPTION_MESSAGE_HEADER, ex.getMessage());
                    ExceptionServices.handle(holder.getResponse(), ex);
                    if (holder.getStatus() != MessageStatus.FINISHED) {
                        holder.setStatus(MessageStatus.ERROR);
                        init.getInterceptor().storeMessage(holder);
                    }
                }
            });
        } else {
            holder.setStatus(MessageStatus.ERROR);
            init.getInterceptor().storeMessage(holder);
        }

    }
}
