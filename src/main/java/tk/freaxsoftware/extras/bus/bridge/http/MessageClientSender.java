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
    
    private ClientConfig config;
    
    private ServerConfig serverConfig;
    
    public void init(ServerConfig serverConfig, ClientConfig config) {
        this.serverConfig = serverConfig;
        this.config = config;
    }

    @Override
    public void receive(MessageHolder message) throws Exception {
        HttpMessageEntry entry = new HttpMessageEntry(message.getMessageId(), message.getHeaders(), message.getContent());
        setupEntry(message, entry);
        HttpMessageEntry response = sendEntry(config.getAddress(), config.getPort(), entry);
        message.getResponse().setContent(response.getContent());
        message.getResponse().setHeaders(response.getHeaders());
    }
    
    private void setupEntry(MessageHolder message, HttpMessageEntry entry) {
        if (message.getOptions().isBroadcast()) {
            entry.getHeaders().put(LocalHttpIds.LOCAL_HTTP_HEADER_MODE, LocalHttpIds.Mode.BROADCAST);
        } else if (message.getOptions().getCallback() != null) {
            entry.getHeaders().put(LocalHttpIds.LOCAL_HTTP_HEADER_MODE, LocalHttpIds.Mode.CALLBACK);
        } else {
            entry.getHeaders().put(LocalHttpIds.LOCAL_HTTP_HEADER_MODE, LocalHttpIds.Mode.SIMPLE);
        }
        entry.getHeaders().put(LocalHttpIds.LOCAL_HTTP_HEADER_NODE_PORT, serverConfig.getHttpPort().toString());
        if (entry.getMessageId().equals(GlobalIds.GLOBAL_SUBSCRIBE)) {
            entry.setMessageId(LocalHttpIds.LOCAL_HTTP_MESSAGE_SUBSCRIBE);
        }
        if (entry.getMessageId().equals(GlobalIds.GLOBAL_UNSUBSCRIBE)) {
            entry.setMessageId(LocalHttpIds.LOCAL_HTTP_MESSAGE_UNSUBSCRIBE);
        }
    }
}
