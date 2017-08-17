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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.freaxsoftware.extras.bus.exceptions.ReceiverRegistrationException;

/**
 * Main message bus entry class.
 * @author Stanislav Nepochatov
 */
public final class MessageBus {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageBus.class);
    
    /**
     * Map of all subscription for messages.
     */
    private static final List<Subscription> subscriptions = new CopyOnWriteArrayList<>();
    
    /**
     * Subscribe receiver for message with following id.
     * @param id message id string;
     * @param receiver message receiver;
     * @throws tk.freaxsoftware.extras.bus.exceptions.ReceiverRegistrationException
     */
    public static void addSubscription(final String id, final Receiver receiver) throws ReceiverRegistrationException {
        if (id == null || receiver == null) {
            throw new ReceiverRegistrationException("Can't processed registration with null references!");
        }
        LOGGER.info("add new subscription for " + id);
        Subscription subscription = getSubscription(id);
        if (subscription == null) {
            subscription = new Subscription(id);
            subscription.addReceiver(receiver);
            subscriptions.add(subscription);
        } else {
            subscription.addReceiver(receiver);
        }
    }
    
    /**
     * Subscribe receiver for multiplie messages ids.
     * @param ids array of ids;
     * @param receiver message receiver;
     * @throws tk.freaxsoftware.extras.bus.exceptions.ReceiverRegistrationException
     */
    public static void addSubscriptions(final String[] ids, final Receiver receiver) throws ReceiverRegistrationException {
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
        LOGGER.info("removing subscription for " + id);
        Subscription subscription = getSubscription(id);
        if (subscription != null) {
            subscription.getReceivers().remove(receiver);
            if (subscription.getReceivers().isEmpty()) {
                subscriptions.remove(subscription);
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
        LOGGER.info("clearing all subscriptions... Reinit subscriptions to proceed.");
        subscriptions.clear();
    }
    
    /**
     * Fire message to the bus.
     * @param messageId id of message;
     * @param args message arguments;
     */
    public static void fire(final String messageId, final Map<String, Object> args) {
        fire(messageId, args, MessageOptions.defaultOptions());
    }
    
    /**
     * Fire message to the bus.
     * @param messageId id of message;
     * @param args message arguments;
     * @param options options for message processing;
     */
    public static void fire(final String messageId, final Map<String, Object> args, final MessageOptions options) {
        if (options == null) {
            throw new IllegalArgumentException("Message options can't be null!");
        }
        LOGGER.info(messageId + " message fired to bus");
        Subscription subscription = getSubscription(messageId);
        if (subscription != null) {
            BlockExecutor.getExecutor().execute(() -> {
                Map<String, Object> result = new HashMap<>();
                if (options.isBroadcast()) {
                    for (Receiver receiver: subscription.getReceivers()) {
                        try {
                            receiver.receive(messageId, args, result);
                        } catch (Exception ex) {
                            LOGGER.error("Receiver " + receiver.getClass().getName() + " for id " + messageId + " throws exception", ex);
                            result.put(GlobalIds.GLOBAL_EXCEPTION, ex);
                        }
                    }
                } else {
                    Receiver singleReceiver = subscription.getRoundRobinIterator().next();
                    try {
                        singleReceiver.receive(messageId, args, result);
                    } catch (Exception ex) {
                        LOGGER.error("Receiver " + singleReceiver.getClass().getName() + " for id " + messageId + " throws exception", ex);
                        result.put(GlobalIds.GLOBAL_EXCEPTION, ex);
                    }
                    if (options.getCallback() != null) {
                        options.getCallback().callback(result);
                    }
                }
            }, options.isAsync());
        }
    }
    
    /**
     * Get subscription for message id;
     * @param messageId id of message to address;
     * @return subscription holder;
     */
    private static Subscription getSubscription(final String messageId) {
        for (Subscription subscription: subscriptions) {
            if (Objects.equals(subscription.getId(), messageId)) {
                return subscription;
            }
        }
        return null;
    }
    
    /**
     * Check result map for tips from bus if message processing was successfull or 
     * halted with error. Result may contains data from receivers so detail inspection required.
     * @param result result map;
     * @return true - no errors founded, false - there is exception stored in map or something else;
     */
    public static Boolean isSuccessful(Map<String, Object> result) {
        return !result.containsKey(GlobalIds.GLOBAL_EXCEPTION) && !result.containsKey(GlobalIds.GLOBAL_ERROR_MESSAGE);
    }
}
