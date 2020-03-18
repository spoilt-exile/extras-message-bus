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
 * Dummy storage interceptor.
 * @author Stanislav Nepochatov
 * @since 5.0
 */
public class DummyStorageInterceptor implements StorageInterceptor {

    @Override
    public void storeMessage(MessageHolder holder) {
        //Do nothing.
    }

    @Override
    public void storeProcessedMessage(MessageHolder holder) {
        //Do nothing.
    }
    
}
