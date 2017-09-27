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

import tk.freaxsoftware.extras.bus.GlobalIds;
import tk.freaxsoftware.extras.bus.MessageHolder;
import tk.freaxsoftware.extras.bus.Receiver;
import tk.freaxsoftware.extras.bus.config.http.ClientConfig;
import tk.freaxsoftware.extras.bus.config.http.ServerConfig;

/**
 * Message http client class and receiver. Used by message bus for sending to server node from recipient node.
 * @author Stanislav Nepochatov
 */
public class MessageClientSender extends AbstractHttpSender implements Receiver {
    
    /**
     * Client config instance.
     */
    private final ClientConfig config;
    
    /**
     * Server config instance.
     */
    private final ServerConfig serverConfig;
    
    /**
     * Default constructor.
     * @param serverConfig instance of server config;
     * @param config instance of client config;
     */
    public MessageClientSender(ServerConfig serverConfig, ClientConfig config) {
        this.serverConfig = serverConfig;
        this.config = config;
    }

    @Override
    public void receive(MessageHolder message) throws Exception {
        // Ignore itself.
        if (message.getContent() == this) {
            return;
        }
        HttpMessageEntry entry = new HttpMessageEntry(message.getMessageId(), message.getHeaders(), message.getContent());
        setupEntry(message, entry);
        HttpMessageEntry response = sendEntry(config.getAddress(), config.getPort(), entry);
        if (response != null) {
            message.getResponse().setHeaders(response.getHeaders());
            message.getResponse().setContent(response.getContent());
        }
    }
    
    /**
     * Setup message entry for sending.
     * @param message message holder;
     * @param entry message entry for sending;
     */
    private void setupEntry(MessageHolder message, HttpMessageEntry entry) {
        setupMessageMode(message, entry);
        //Add header with this node server port for back comminication.
        entry.getHeaders().put(LocalHttpIds.LOCAL_HTTP_HEADER_NODE_PORT, serverConfig.isNested() ? String.valueOf(spark.Spark.port()) : serverConfig.getHttpPort().toString());
        //Override if subscription message.
        if (entry.getMessageId().equals(GlobalIds.GLOBAL_SUBSCRIBE)) {
            entry.setMessageId(LocalHttpIds.LOCAL_HTTP_MESSAGE_SUBSCRIBE);
            entry.setContent(null);
            entry.setFullTypeName(null);
            entry.setTypeName(null);
            entry.getHeaders().put(LocalHttpIds.LOCAL_HTTP_HEADER_MODE, LocalHttpIds.Mode.SIMPLE.name());
        }
        //Override if unsubscription message.
        if (entry.getMessageId().equals(GlobalIds.GLOBAL_UNSUBSCRIBE)) {
            entry.setMessageId(LocalHttpIds.LOCAL_HTTP_MESSAGE_UNSUBSCRIBE);
            entry.setContent(null);
            entry.setFullTypeName(null);
            entry.setTypeName(null);
            entry.getHeaders().put(LocalHttpIds.LOCAL_HTTP_HEADER_MODE, LocalHttpIds.Mode.SIMPLE.name());
        }
    }
}
