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

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.freaxsoftware.extras.bus.MessageHolder;
import tk.freaxsoftware.extras.bus.Receiver;

/**
 * Message http messaging sender and receiver. Used for send messages from server node to subscribers.
 * @author Stanislav Nepochatov
 */
public class MessagePeerSender extends AbstractHttpSender implements Receiver {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MessagePeerSender.class);
    
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
     * Local date of the last heartbeat of the node.
     */
    private LocalDateTime beat;
    
    /**
     * Lock for heart beat date.
     */
    private final Lock beatLock;

    /**
     * Default constructor.
     * @param address ip address or host;
     * @param port http port number;
     */
    public MessagePeerSender(String address, Integer port) {
        this.address = address;
        this.port = port;
        this.subscriptions = new ConcurrentHashSet<>();
        this.beat = LocalDateTime.now();
        this.beatLock = new ReentrantLock();
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
    
    /**
     * Update beat timestamp on current receiver.
     */
    public void beat() {
        this.beatLock.lock();
        try {
            this.beat = LocalDateTime.now();
            LOGGER.info("Update beat: {}", this.beat);
        } finally {
            this.beatLock.unlock();
        }
    }
    
    /**
     * Get heart beat expiration status.
     * @param beatMaxAge max allowable age of heartbeat;
     * @return true if heartbeat were not refreshed since last check / false if heartbeat were refreshed;
     */
    public Boolean isBeatExpired(Integer beatMaxAge) {
        LocalDateTime now = LocalDateTime.now();
        long diff = 0;
        this.beatLock.lock();
        try {
            diff = ChronoUnit.SECONDS.between(beat, now);
            LOGGER.info("Beat: {}", diff);
        } finally {
            this.beatLock.unlock();
        }
        return beatMaxAge < diff;
    }
    
    public String[] getSubscriptions() {
        return this.subscriptions.toArray(new String[this.subscriptions.size()]);
    }

    @Override
    public void receive(MessageHolder message) throws Exception {
        if (subscriptions.contains(message.getMessageId())
                && !(Objects.equals(message.getHeaders().get(LocalHttpIds.LOCAL_HTTP_HEADER_NODE_IP), this.address) 
                && Objects.equals(message.getHeaders().get(LocalHttpIds.LOCAL_HTTP_HEADER_NODE_PORT), this.port))) {
            LOGGER.debug(String.format("Sending message %s to subscriber node %s on port %d", message.getMessageId(), address, port));
            HttpMessageEntry entry = new HttpMessageEntry(message.getMessageId(), message.getHeaders(), message.getContent());
            setupMessageMode(message, entry);
            HttpMessageEntry response = sendEntry(address, port, entry);
            if (response != null) {
                message.getResponse().setContent(response.getContent());
                message.getResponse().setHeaders(response.getHeaders());
            }
        }
    }
    
}
