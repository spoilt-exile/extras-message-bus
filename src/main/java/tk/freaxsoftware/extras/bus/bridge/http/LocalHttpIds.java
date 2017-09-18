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

package tk.freaxsoftware.extras.bus.bridge.http;

/**
 * Local id class related to http bridge.
 * @author Stanislav Nepochatov
 */
public class LocalHttpIds {
    
    /**
     * Local HTTP header for bridge.
     */
    protected static final String LOCAL_HTTP_HEADER_CALLBACK = "Local.Http.Header.Mode";
    
    /**
     * Enum for HTTP bridging mode.
     */
    public static enum Mode {
        
        /**
         * Async, single, no callback.
         */
        SIMPLE,
        
        /**
         * Sync, single with callback.
         */
        CALLBACK,
        
        /**
         * Async, broadcast without callback.
         */
        BROADCAST;
    }
    
}
