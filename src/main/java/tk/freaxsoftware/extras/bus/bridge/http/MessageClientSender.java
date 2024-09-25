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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.freaxsoftware.extras.bus.GlobalCons;
import tk.freaxsoftware.extras.bus.MessageBus;
import tk.freaxsoftware.extras.bus.MessageHolder;
import tk.freaxsoftware.extras.bus.MessageOptions;
import tk.freaxsoftware.extras.bus.MessageStatus;
import tk.freaxsoftware.extras.bus.Receiver;
import tk.freaxsoftware.extras.bus.config.http.ClientConfig;
import tk.freaxsoftware.extras.bus.config.http.ServerConfig;

/**
 * Message http client class and receiver. Used by message bus for sending to server node from recipient node.
 * @author Stanislav Nepochatov
 */
public class MessageClientSender extends AbstractHttpSender implements Receiver {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageClientSender.class);
    
    /**
     * Client config instance.
     */
    private final ClientConfig config;
    
    /**
     * Server config instance.
     */
    private final ServerConfig serverConfig;
    
    private final Set<String> subscriptions = Collections.synchronizedSet(new HashSet());
    
    private ExecutorService threadService = Executors.newSingleThreadExecutor();
    
    /**
     * Default constructor.
     * @param serverConfig instance of server config;
     * @param config instance of client config;
     */
    public MessageClientSender(ServerConfig serverConfig, ClientConfig config) {
        this.serverConfig = serverConfig;
        this.config = config;
        LOGGER.info(String.format("Init connection to node %s on port %d", config.getAddress(), config.getPort()));
        
        if (config.getHeartbeatRate() != null && config.getHeartbeatRate() > 0) {
            LOGGER.info(String.format("Init heartbeat %d", config.getHeartbeatRate()));
            threadService.submit(new Runnable() {

                public void run() {
                    while (true) {
                        try {
                            MessageBus.fire(LocalHttpCons.L_HTTP_HEARTBEAT_TOPIC, subscriptions, MessageOptions.Builder.newInstance().sync().broadcast().header(LocalHttpCons.L_HTTP_NODE_REGISTERED_TYPE_HEADER, LocalHttpCons.L_HTTP_HEARTBEAT_TYPE_NAME).build());
                        } catch (Exception ex) {
                            LOGGER.error("Can't send heartbeat, maybe server is offline, reinit connection", ex);
                        }
                        try {
                            Thread.sleep(config.getHeartbeatRate() * 1000);
                        } catch (InterruptedException ex) {
                            LOGGER.error("Killer thread interrupted!", ex);
                        }
                    }
                }
            });
        }
    }

    @Override
    public synchronized void receive(MessageHolder message) throws Exception {
        // Ignore itself.
        if (message.getContent() == this) {
            return;
        }
        HttpMessageEntry entry = new HttpMessageEntry(message);
        setupEntry(message, entry);
        LOGGER.debug(String.format("Sending message %s to node %s on port %d", message.getTopic(), config.getAddress(), config.getPort()));
        HttpMessageEntry response = sendEntry(config.getAddress(), config.getPort(), entry);
        if (response != null) {
            message.getResponse().setHeaders(response.getHeaders());
            message.getResponse().setContent(response.getContent());
        } else if (message.getHeaders().containsKey(LocalHttpCons.L_HTTP_NODE_SYNC_CALL_HEADER)) {
            message.setStatus(MessageStatus.REMOTE_PROCESSING);
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
        entry.getHeaders().put(LocalHttpCons.L_HTTP_NODE_PORT_HEADER, serverConfig.getHttpPort().toString());
        //Override if subscription message.
        if (entry.getTopic().equals(GlobalCons.G_SUBSCRIBE_TOPIC)) {
            subscriptions.add((String) entry.getHeaders().get(GlobalCons.G_SUBSCRIPTION_DEST_HEADER));
            entry.setTopic(LocalHttpCons.L_HTTP_SUBSCRIBE_TOPIC);
            entry.setContent(null);
            entry.setFullTypeName(null);
            entry.setTypeName(null);
            entry.getHeaders().put(LocalHttpCons.L_HTTP_MODE_HEADER, LocalHttpCons.Mode.ASYNC.name());
        }
        //Override if unsubscription message.
        if (entry.getTopic().equals(GlobalCons.G_UNSUBSCRIBE_TOPIC)) {
            subscriptions.remove((String) entry.getHeaders().get(GlobalCons.G_SUBSCRIPTION_DEST_HEADER));
            entry.setTopic(LocalHttpCons.L_HTTP_UNSUBSCRIBE_TOPIC);
            entry.setContent(null);
            entry.setFullTypeName(null);
            entry.setTypeName(null);
            entry.getHeaders().put(LocalHttpCons.L_HTTP_MODE_HEADER, LocalHttpCons.Mode.ASYNC.name());
        }
    }
}
