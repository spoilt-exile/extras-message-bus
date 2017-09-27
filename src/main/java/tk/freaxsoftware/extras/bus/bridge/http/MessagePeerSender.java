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

import java.util.Set;
import org.eclipse.jetty.util.ConcurrentHashSet;
import tk.freaxsoftware.extras.bus.MessageHolder;
import tk.freaxsoftware.extras.bus.Receiver;

/**
 * Message http messaging sender and receiver. Used for send messages from server node to subscribers.
 * @author Stanislav Nepochatov
 */
public class MessagePeerSender extends AbstractHttpSender implements Receiver {
    
    /**
     * Node address.
     */
    private final String address;
    
    /**
     * Node port.
     */
    private final Integer port;
    
    /**
     * Node subscriptions.
     */
    private final Set<String> subscriptions;

    /**
     * Default constructor.
     * @param address ip address or host;
     * @param port http port number;
     */
    public MessagePeerSender(String address, Integer port) {
        this.address = address;
        this.port = port;
        this.subscriptions = new ConcurrentHashSet<>();
    }
    
    /**
     * Add subscription.
     * @param id message id;
     */
    public void addSubscription(String id) {
        this.subscriptions.add(id);
    }
    
    /**
     * Remove subscription.
     * @param id message id;
     */
    public void removeSubscription(String id) {
        this.subscriptions.remove(id);
    }
    
    /**
     * Return wheather or not current receiver is empty.
     * @return true if no subscriptions inside / false if there is still some subscriptions;
     */
    public Boolean isEmpty() {
        return this.subscriptions.isEmpty();
    }

    @Override
    public void receive(MessageHolder message) throws Exception {
        if (subscriptions.contains(message.getMessageId())) {
            HttpMessageEntry entry = new HttpMessageEntry(message.getMessageId(), message.getHeaders(), message.getContent());
            setupMessageMode(message, entry);
            HttpMessageEntry response = sendEntry(address, port, entry);
            message.getResponse().setContent(response.getContent());
            message.getResponse().setHeaders(response.getHeaders());
        }
    }
    
}
