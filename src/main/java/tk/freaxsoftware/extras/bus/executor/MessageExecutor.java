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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.freaxsoftware.extras.bus.MessageBusInit;
import tk.freaxsoftware.extras.bus.MessageHolder;
import tk.freaxsoftware.extras.bus.Subscription;

/**
 * Message executor for permforming processing of messages.
 * @author Stanislav Nepochatov
 * @since 5.0
 */
public abstract class MessageExecutor {
    
    protected final static Logger LOGGER = LoggerFactory.getLogger(MessageExecutor.class);
    
    protected final MessageHolder holder;
    protected final Subscription subscription;
    protected final MessageBusInit init;

    public MessageExecutor(MessageHolder holder, Subscription subscription, MessageBusInit init) {
        this.holder = holder;
        this.subscription = subscription;
        this.init = init;
    }
    
    /**
     * Main execution method.
     */
    public abstract void exec();
    
    /**
     * Check if new arrived message is already present in message storage. May be used to skip duplicates.
     * @param uuid message unique uuid;
     * @return true if message is present / false if no message in storage;
     */
    protected boolean isMessagePresent(String uuid) {
        return init.getInterceptor().getStorage().getMessageById(uuid).isPresent();
    }
}
