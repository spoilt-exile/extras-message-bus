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

package tk.freaxsoftware.extras.bus;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main message bus entry class.
 * @author Stanislav Nepochatov
 */
public final class MessageBus {
    
    private static final Logger logger = LoggerFactory.getLogger(MessageBus.class);
    
    /**
     * Map of all subscription for messages.
     */
    private static Map<String, Subscription> subscriptionMap = new HashMap<>();
    
    /**
     * Thread pool executor.
     */
    private static final ExecutorService threadService = Executors.newFixedThreadPool(4);
    
    /**
     * Subscribe receiver for message with following id.
     * @param id message id string;
     * @param receiver message receiver;
     */
    public static void addSubscription(final String id, final Receiver receiver) {
        logger.info("add new subscription for " + id);
        if (subscriptionMap.containsKey(id)) {
            Subscription currentSubscription = subscriptionMap.get(id);
            currentSubscription.addReceiver(receiver);
        } else {
            logger.debug("creating new subscription instance for " + id);
            Subscription newSubscription = new Subscription(id);
            newSubscription.addReceiver(receiver);
            subscriptionMap.put(id, newSubscription);
        }
    }
    
    /**
     * Subscribe receiver for multiplie messages ids.
     * @param ids array of ids;
     * @param receiver message receiver;
     */
    public static void addSubscriptions(final String[] ids, final Receiver receiver) {
        for (String id: ids) {
            addSubscription(id, receiver);
        }
    }
    
    /**
     * Unsubscribe following receiver from message id.
     * @param id message id;
     * @param receiver the same receiver instance which were using during subscription;
     */
    public static void removeSubscription(final String id, final Receiver receiver) {
        logger.info("removing subscription for " + id);
        if (subscriptionMap.containsKey(id)) {
            Subscription currentSubscription = subscriptionMap.get(id);
            currentSubscription.removeReceiver(receiver);
            if (currentSubscription.getReceivers().isEmpty()) {
                logger.debug("deleting subscription record for " + id);
                subscriptionMap.remove(id);
            }
        }
    }
    
    /**
     * Unsubscribe following receiver from following message ids.
     * @param ids array of message ids to unsubscribe;
     * @param receiver the same receiver instance which were using during subscription;
     */
    public static void removeSubscriptions(final String[] ids, final Receiver receiver) {
        for (String id: ids) {
            removeSubscription(id, receiver);
        }
    }
    
    /**
     * Clear all subscriptions. This action reset whole message bus. Use with care.
     */
    public static void clearBus() {
        logger.info("clearing all subscriptions... Reinit subscriptions to proceed.");
        subscriptionMap.clear();
    }
    
    /**
     * Fire message to the bus. SYNC METHOD!
     * @param messageId id of message;
     * @param args message arguments;
     * @param callback post-execution callback;
     */
    public static void fireMessageSync(final String messageId, final Map<String, Object> args, final Callback callback) {
        logger.info(messageId + " message fired to bus");
        if (subscriptionMap.containsKey(messageId)) {
            Subscription currentSubscription = subscriptionMap.get(messageId);
            Map<String, Object> result = new HashMap<>();
            for (Receiver receiver: currentSubscription.getReceivers()) {
                receiver.receive(messageId, args, result);
            }
            if (callback != null) {
                callback.callback(result);
            }
        }
    }
    
    /**
     * Fire message to the bus. ASYNC METHOD!
     * @param messageId id of message;
     * @param args message arguments;
     * @param callback post-execution callback;
     */
    public static void fireMessage(final String messageId, final Map<String, Object> args, final Callback callback) {
        logger.info("start async thread for message " + messageId);
        threadService.submit(new Runnable() {

            @Override
            public void run() {
                fireMessageSync(messageId, args, callback);
            }
        });
    }
}
