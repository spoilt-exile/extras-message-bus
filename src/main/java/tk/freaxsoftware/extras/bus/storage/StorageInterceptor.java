/*
 * This file is part of MessageBus library.
 * 
 * Copyright (C) 2015 Freax Software
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
package tk.freaxsoftware.extras.bus.storage;

import tk.freaxsoftware.extras.bus.MessageHolder;

/**
 * Storage interceptor ties itself with message bus lifecycle to store messages in certain cases.
 * @author Stanislav Nepochatov
 * @since 5.0
 */
public interface StorageInterceptor {
    
    /**
     * Saves messages to storage. May init additional processing.
     * @param holder message to save;
     */
    void storeMessage(MessageHolder holder);
    
    /**
     * Saves messaged successful processing.
     * @param holder message to save;
     */
    void storeProcessedMessage(MessageHolder holder);
}
