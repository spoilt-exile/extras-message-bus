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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.freaxsoftware.extras.bus.exceptions.NoSubscriptionMessageException;
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
    private static final Map<String, Subscription> subscriptionMap = new ConcurrentHashMap<>();
    
    /**
     * Thread pool executor.
     */
    private static final ExecutorService threadService = Executors.newFixedThreadPool(4);
    
    /**
     * Subscribe receiver for message with following id.
     * @param id message id string;
     * @param receiver message receiver;
     */
    public static void addSubscription(final String id, final Receiver receiver) throws ReceiverRegistrationException {
        if (id == null || receiver == null) {
            throw new ReceiverRegistrationException("Can't processed registration with null references!");
        }
        LOGGER.info("add new subscription for " + id);
        if (subscriptionMap.containsKey(id)) {
            Subscription currentSubscription = subscriptionMap.get(id);
            currentSubscription.addReceiver(receiver);
        } else {
            LOGGER.debug("creating new subscription instance for " + id);
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
        if (subscriptionMap.containsKey(id)) {
            Subscription currentSubscription = subscriptionMap.get(id);
            currentSubscription.removeReceiver(receiver);
            if (currentSubscription.getReceivers().isEmpty()) {
                LOGGER.debug("deleting subscription record for " + id);
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
        LOGGER.info("clearing all subscriptions... Reinit subscriptions to proceed.");
        subscriptionMap.clear();
    }
    
    /**
     * Fire message to the bus. SYNC METHOD!
     * @param messageId id of message;
     * @param args message arguments;
     * @param callback post-execution callback;
     */
    public static void fireMessageSync(final String messageId, final Map<String, Object> args, final Callback callback) {
        LOGGER.info(messageId + " message fired to bus");
        if (subscriptionMap.containsKey(messageId)) {
            Subscription currentSubscription = subscriptionMap.get(messageId);
            Map<String, Object> result = new HashMap<>();
            for (Receiver receiver: currentSubscription.getReceivers()) {
                try {
                    receiver.receive(messageId, args, result);
                } catch (Exception ex) {
                    LOGGER.error("Receiver " + receiver.getClass().getName() + " for id " + messageId + " throws exception", ex);
                    result.put(GlobalIds.GLOBAL_EXCEPTION, ex);
                }
            }
            if (callback != null) {
                callback.callback(result);
            }
        }
    }
    
    /**
     * Fire message to the bus with arg helper. SYNC METHOD!
     * @param messageId id of message;
     * @param argsHelper message arguments helper;
     * @param callback post-execution callback;
     */
    public static void fireMessageSyncHelped(final String messageId, final ArgHelper argsHelper, final Callback callback) {
        fireMessageSync(messageId, argsHelper.getArgs(), callback);
    }
    
    /**
     * Fire message to the bus. ASYNC METHOD!
     * @param messageId id of message;
     * @param args message arguments;
     * @param callback post-execution callback;
     */
    public static void fireMessage(final String messageId, final Map<String, Object> args, final Callback callback) {
        LOGGER.info("start async thread for message " + messageId);
        threadService.submit(() -> {
            fireMessageSync(messageId, args, callback);
        });
    }
    
    /**
     * Fire message to the bus with arg helper. ASYNC METHOD!
     * @param messageId id of message;
     * @param argHelper message arguments helper;
     * @param callback post-execution callback;
     */
    public static void fireMessageHelped(final String messageId, final ArgHelper argHelper, final Callback callback) {
        fireMessage(messageId, argHelper.getArgs(), callback);
    }
    
    /**
     * Fire messageto the bus with additional checking. If there is no subscribers, 
     * then {@code NoSubscriptionMessageException} will be throwned. Sync method.
     * @param messageId id of message;
     * @param args message arguments;
     * @param callback post-execution callback;
     * @throws tk.freaxsoftware.extras.bus.exceptions.NoSubscriptionMessageException
     * @see NoSubscriptionMessageException
     */
    public static void fireMessageSyncChecked(final String messageId, final Map<String, Object> args, final Callback callback) throws NoSubscriptionMessageException {
        if (subscriptionMap.containsKey(messageId)) {
            fireMessageSync(messageId, args, callback);
        } else {
            throw new NoSubscriptionMessageException("Message " + messageId + "has no subscriptions on this bus!");
        }
    }
    
    /**
     * Fire messageto the bus with additional checking and arg helper. If there is no subscribers, 
     * then {@code NoSubscriptionMessageException} will be throwned. Sync method.
     * @param messageId id of message;
     * @param argHelper message arguments helper;
     * @param callback post-execution callback;
     * @throws tk.freaxsoftware.extras.bus.exceptions.NoSubscriptionMessageException
     * @see NoSubscriptionMessageException
     */
    public static void fireMessageSyncCheckedHelped(final String messageId, final ArgHelper argHelper, final Callback callback) throws NoSubscriptionMessageException {
        fireMessageSyncChecked(messageId, argHelper.getArgs(), callback);
    }
    
    /**
     * Fire message to the bus with additional check. ASYNC METHOD!
     * @param messageId id of message;
     * @param args message arguments;
     * @param callback post-execution callback;
     */
    public static void fireMessageChecked(final String messageId, final Map<String, Object> args, final Callback callback) {
        LOGGER.info("start async thread for message " + messageId);
        threadService.submit(() -> {
            try {
                fireMessageSyncChecked(messageId, args, callback);
            } catch (NoSubscriptionMessageException nox) {
                LOGGER.error("Can't process " + messageId + ": no subscriptions!");
                if (callback != null) {
                    Map<String, Object> result = new HashMap<>();
                    result.put(GlobalIds.GLOBAL_EXCEPTION, nox);
                    callback.callback(result);
                }
            }
        });
    }
    
    /**
     * Fire message to the bus with additional check with arg helper. ASYNC METHOD!
     * @param messageId id of message;
     * @param argHelper message arguments helper;
     * @param callback post-execution callback;
     */
    public static void fireMessageCheckedHelper(final String messageId, final ArgHelper argHelper, final Callback callback) {
        fireMessageChecked(messageId, argHelper.getArgs(), callback);
    }
    
    /**
     * Check result map for tips from bus if message processing was successfull or 
     * halted with error. Result may contains data from receivers so detail inspection required.
     * @param result result map;
     * @return true - no errors founded, false - there is exception stored in map or something else;
     */
    public static Boolean isSuccessful(Map<String, Object> result) {
        return !result.containsKey(GlobalIds.GLOBAL_EXCEPTION) || !result.containsKey(GlobalIds.GLOBAL_ERROR_MESSAGE);
    }
}
