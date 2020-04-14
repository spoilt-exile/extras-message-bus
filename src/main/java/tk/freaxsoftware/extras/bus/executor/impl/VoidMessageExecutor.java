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
import tk.freaxsoftware.extras.bus.MessageOptions;
import tk.freaxsoftware.extras.bus.Subscription;
import tk.freaxsoftware.extras.bus.executor.MessageExecutor;

/**
 * Default executor for VOID delivery.
 * @author Stanislav Nepochatov
 * @see MessageOptions.DeliveryPolicy#VOID
 * @since 5.0
 */
public class VoidMessageExecutor extends MessageExecutor {

    public VoidMessageExecutor(MessageHolder holder, Subscription subscription, MessageBusInit init) {
        super(holder, subscription, init);
    }

    @Override
    public void exec() {
        MessageContextHolder.setContext(new MessageContext(holder.getTrxId()));
        if (subscription != null) {
            subscription.getReceiversByMode(holder.getOptions().isBroadcast()).forEach(rc -> {
                try {
                    rc.receive(holder);
                } catch (Exception ex) {
                    LOGGER.error("Receiver " + rc.getClass().getName() + " for topic " + holder.getTopic() + " throws exception", ex);
                    holder.getResponse().getHeaders().put(GlobalCons.G_EXCEPTION_HEADER, ex.getClass().getCanonicalName());
                    holder.getResponse().getHeaders().put(GlobalCons.G_EXCEPTION_MESSAGE_HEADER, ex.getMessage());
                }
            });
        }
    }
}
