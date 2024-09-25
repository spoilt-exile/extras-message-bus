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

import com.google.gson.reflect.TypeToken;
import java.util.Set;

/**
 * Local constants class related to http bridge.
 * @author Stanislav Nepochatov
 */
public class LocalHttpCons {
    
    /**
     * Url for listening on messages.
     */
    protected static final String L_HTTP_URL = "/broker/message";
    
    /**
     * Url for listening on sync calls.
     */
    protected static final String L_HTTP_SYNC_URL = "/broker/sync";
    
    /**
     * Message notification of subscription over HTTP.
     */
    public static final String L_HTTP_SUBSCRIBE_TOPIC = "Local.Http.Message.Subscribe";
    
    /**
     * Message notification of unsubscription over HTTP.
     */
    public static final String L_HTTP_UNSUBSCRIBE_TOPIC = "Local.Http.Message.Unsubscribe";
    
    /**
     * Message notification for node heartbeat over HTTP.
     */
    public static final String L_HTTP_HEARTBEAT_TOPIC = "Local.Http.Message.HeartBeat";
    
    /**
     * Type name for content of heartbeat message over HTTP (copy of subscriptions).
     */
    public static final String L_HTTP_HEARTBEAT_TYPE_NAME = "HeartBeatSet";
    
    /**
     * Type token for content of heartbeat message over HTTP (copy of subscriptions).
     */
    public static final TypeToken L_HTTP_HEARTBEAT_TYPE_TOKEN = new TypeToken<Set<String>>() {};
    
    /**
     * Message notification to init cross connections.
     */
    public static final String L_HTTP_CROSS_NODE_TOPIC = "Local.Http.Message.CrossNode";
    
    /**
     * Notifies when central node detects node with possible cross connection for current node. Dynamic format.
     */
    public static final String L_HTTP_CROSS_NODE_UP_TOPIC_FORMAT = "Local.Http.Message.CrossNodeUp.%s:%s";
    
    /**
     * Notifies when central node detects node with possible cross connection for current node.
     */
    public static final String L_HTTP_CROSS_NODE_UP_TOPIC = "Local.Http.Message.CrossNodeUp";
    
    /**
     * Notifies when central node detects node with cross connections is down.
     */
    public static final String L_HTTP_CROSS_NODE_DOWN_TOPIC = "Local.Http.Message.CrossNodeDown";
    
    /**
     * Local HTTP header for bridge mode.
     */
    public static final String L_HTTP_MODE_HEADER = "Local.Http.Header.Mode";
    
    /**
     * Local HTTP header for node IP address.
     */
    public static final String L_HTTP_NODE_IP_HEADER = "Local.Http.Header.NodeIP";
    
    /**
     * Local HTTP header for node port number.
     */
    public static final String L_HTTP_NODE_PORT_HEADER = "Local.Http.Header.NodePort";
    
    /**
     * Local HTTP header for enabling sync call for node to refresh state of message.
     */
    public static final String L_HTTP_NODE_SYNC_CALL_HEADER = "Local.Http.Header.SyncCall";
    
    /**
     * Local HTTP header registered type id.
     */
    public static final String L_HTTP_NODE_REGISTERED_TYPE_HEADER = "Local.Http.Header.RegisteredType";
    
    /**
     * Enum for HTTP bridging mode.
     */
    public static enum Mode {
        
        /**
         * Async, single, no callback.
         */
        ASYNC,
        
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
