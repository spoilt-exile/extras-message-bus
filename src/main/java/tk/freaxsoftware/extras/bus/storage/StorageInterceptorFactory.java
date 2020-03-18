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
package tk.freaxsoftware.extras.bus.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for storage interceptor.
 * @author Stanislav Nepochatov
 * @since 5.0
 */
public class StorageInterceptorFactory {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageInterceptorFactory.class);
    
    public static StorageInterceptor interceptor(StorageConfig config) {
        StorageInterceptor interceptor = null;
        if (config != null && config.isValid()) {
            try {
                interceptor = new DefaultStorageInterceptor(config);
            } catch (StorageInitException ex) {
                LOGGER.error("Error during init of storage, fallback to dummy implementation.", ex);
            }
        }
        
        if (interceptor == null) {
            interceptor = new DummyStorageInterceptor();
        }
        return interceptor;
    }
    
}
