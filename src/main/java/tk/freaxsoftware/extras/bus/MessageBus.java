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

/**
 * Main message bus entry class.
 * @author Stanislav Nepochatov
 */
public final class MessageBus {
    
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
        if (subscriptionMap.containsKey(id)) {
            Subscription currentSubscription = subscriptionMap.get(id);
            currentSubscription.addReceiver(receiver);
        } else {
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
     * Fire message to the bus. SYNC METHOD!
     * @param messageId id of message;
     * @param args message arguments;
     * @param callback post-execution callback;
     */
    public static void fireMessage(final String messageId, final Map<String, Object> args, final Callback callback) {
        if (subscriptionMap.containsKey(messageId)) {
            Subscription currentSubscription = subscriptionMap.get(messageId);
            Map<String, Object> result = new HashMap<>();
            for (Receiver receiver: currentSubscription.getReceivers()) {
                receiver.receive(messageId, args, result);
            }
            callback.callback(result);
        }
    }
    
    /**
     * Fire message to the bus. ASYNC METHOD!
     * @param messageId id of message;
     * @param args message arguments;
     * @param callback post-execution callback;
     */
    public static void fireMessageThreaded(final String messageId, final Map<String, Object> args, final Callback callback) {
        threadService.submit(new Runnable() {

            @Override
            public void run() {
                fireMessage(messageId, args, callback);
            }
        });
    }
}
