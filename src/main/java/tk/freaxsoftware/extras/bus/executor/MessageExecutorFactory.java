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
package tk.freaxsoftware.extras.bus.executor;

import tk.freaxsoftware.extras.bus.MessageBusInit;
import tk.freaxsoftware.extras.bus.MessageHolder;
import tk.freaxsoftware.extras.bus.Subscription;
import tk.freaxsoftware.extras.bus.executor.impl.CallMessageExecutor;
import tk.freaxsoftware.extras.bus.executor.impl.StoreMessageExecutor;
import tk.freaxsoftware.extras.bus.executor.impl.VoidMessageExecutor;

/**
 * Factory for building message executors blocks.
 * @author Stanislav Nepochatov
 * @since 5.0
 */
public class MessageExecutorFactory {
    
    public static MessageExecutor newExecutor(MessageHolder holder, Subscription subscription, MessageBusInit init) {
        switch (holder.getOptions().getDeliveryPolicy()) {
            case VOID:
                return new VoidMessageExecutor(holder, subscription, init);
            case CALL:
                return new CallMessageExecutor(holder, subscription, init);
            case STORE:
                return new StoreMessageExecutor(holder, subscription, init);
        }
        return null;
    }
    
}
