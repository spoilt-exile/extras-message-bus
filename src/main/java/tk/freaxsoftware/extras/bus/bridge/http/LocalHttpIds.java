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
     * Url for listening on messages.
     */
    protected static final String LOCAL_HTTP_URL = "/broker/message";
    
    /**
     * Message notification of subscription over HTTP.
     */
    public static final String LOCAL_HTTP_MESSAGE_SUBSCRIBE = "Local.Http.Message.Subscribe";
    
    /**
     * Message notification of unsubscription over HTTP.
     */
    public static final String LOCAL_HTTP_MESSAGE_UNSUBSCRIBE = "Local.Http.Message.Unsubscribe";
    
    /**
     * Message notification for node heartbeat over HTTP.
     */
    public static final String LOCAL_HTTP_MESSAGE_HEARTBEAT = "Local.Http.Message.HeartBeat";
    
    /**
     * Local HTTP header for bridge mode.
     */
    protected static final String LOCAL_HTTP_HEADER_MODE = "Local.Http.Header.Mode";
    
    /**
     * Local HTTP header for node IP address.
     */
    protected static final String LOCAL_HTTP_HEADER_NODE_IP = "Local.Http.Header.NodeIP";
    
    /**
     * Local HTTP header for node port number.
     */
    protected static final String LOCAL_HTTP_HEADER_NODE_PORT = "Local.Http.Header.NodePort";
    
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
