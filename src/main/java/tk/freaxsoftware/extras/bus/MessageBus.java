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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
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
    private static final List<Subscription> subscriptions = new CopyOnWriteArrayList<>();
    
    /**
     * Message bus init util.
     */
    private static final MessageBusInit init = new MessageBusInit();
    
    /**
     * Subscribe receiver for message with following topic.
     * @param topic message topic destination;
     * @param receiver message receiver;
     */
    public static void addSubscription(final String topic, final Receiver receiver) {
        init();
        if (topic == null || receiver == null) {
            throw new ReceiverRegistrationException("Can't processed registration with null references!");
        }
        LOGGER.info("add new subscription for " + topic);
        Subscription subscription = getSubscription(topic);
        if (subscription == null) {
            subscription = new Subscription(topic);
            subscription.addReceiver(receiver);
            subscriptions.add(subscription);
        } else {
            subscription.addReceiver(receiver);
        }
        fire(GlobalCons.G_SUBSCRIBE_TOPIC, receiver, 
                HeaderBuilder.newInstance().put(GlobalCons.G_SUBSCRIPTION_DEST_HEADER, topic).build(), 
                MessageOptions.Builder.newInstance().async().broadcast().build());
    }
    
    /**
     * Subscribe receiver for multiplie messages topics.
     * @param topics array of topic destinations;
     * @param receiver message receiver;
     */
    public static void addSubscriptions(final String[] topics, final Receiver receiver) {
        for (String topic: topics) {
            addSubscription(topic, receiver);
        }
    }
    
    /**
     * Unsubscribe following receiver from message topic.
     * @param topic message topic destination;
     * @param receiver the same receiver instance which were using during subscription;
     */
    public static void removeSubscription(final String topic, final Receiver receiver) {
        LOGGER.info("removing subscription for " + topic);
        init();
        Subscription subscription = getSubscription(topic);
        if (subscription != null) {
            subscription.getReceivers().remove(receiver);
            if (subscription.getReceivers().isEmpty()) {
                subscriptions.remove(subscription);
            }
        }
        fire(GlobalCons.G_UNSUBSCRIBE_TOPIC, receiver, 
                HeaderBuilder.newInstance().put(GlobalCons.G_SUBSCRIPTION_DEST_HEADER, topic).build(), 
                MessageOptions.Builder.newInstance().async().broadcast().build());
    }
    
    /**
     * Unsubscribe following receiver from following message topics.
     * @param topics array of topic destinations;
     * @param receiver the same receiver instance which were using during subscription;
     */
    public static void removeSubscriptions(final String[] topics, final Receiver receiver) {
        for (String topic: topics) {
            removeSubscription(topic, receiver);
        }
    }
    
    /**
     * Clear all subscriptions. This action reset whole message bus. Use with care.
     */
    public static void clearBus() {
        init();
        LOGGER.info("clearing all subscriptions... Reinit subscriptions to proceed.");
        subscriptions.clear();
    }
    
    /**
     * Fire message to the bus.
     * @param <T> type of content;
     * @param topic destination of message;
     * @param content message content;
     */
    public static <T> void fire(final String topic, final T content) {
        fire(topic, content, null, MessageOptions.defaultOptions());
    }
    
    /**
     * Fire message to the bus.
     * @param <T> type of content;
     * @param topic destination of message;
     * @param headers message headers;
     * @param options options for message processing;
     */
    public static <T> void fire(final String topic, final Map<String, String> headers, final MessageOptions options) {
        fire(topic, null, headers, options);
    }
    
    /**
     * Fire message to the bus.
     * @param <T> type of content;
     * @param topic destination of message;
     * @param content message content;
     * @param headers message headers;
     * @param options options for message processing;
     */
    public static <T> void fire(final String topic, final T content, final Map<String, String> headers, final MessageOptions options) {
        init();
        if (options == null) {
            throw new IllegalArgumentException("Message options can't be null!");
        }
        LOGGER.info("Message with topic {} fired to bus", topic);
        Subscription subscription = getSubscription(topic);
        if (subscription != null) {
            init.getExecutor().execute(() -> {
                if (options.isBroadcast()) {
                    MessageHolder<T> holder = new MessageHolder<>(topic, options, content);
                    if (headers != null) {
                        holder.setHeaders(headers);
                    }
                    for (Receiver receiver: subscription.getReceivers()) {
                        try {
                            receiver.receive(holder);
                        } catch (Exception ex) {
                            LOGGER.error("Receiver " + receiver.getClass().getName() + " for topic " + topic + " throws exception", ex);
                            holder.getResponse().getHeaders().put(GlobalCons.G_EXCEPTION_HEADER, ex.getClass().getCanonicalName());
                        }
                    }
                } else {
                    MessageHolder<T> holder = new MessageHolder<>(topic, options, content);
                    if (headers != null) {
                        holder.setHeaders(headers);
                    }
                    Receiver singleReceiver = subscription.getRoundRobinIterator().next();
                    try {
                        singleReceiver.receive(holder);
                    } catch (Exception ex) {
                        LOGGER.error("Receiver " + singleReceiver.getClass().getName() + " for topic " + topic + " throws exception", ex);
                        holder.getResponse().getHeaders().put(GlobalCons.G_EXCEPTION_HEADER, ex.getClass().getCanonicalName());
                    }
                    if (options.getCallback() != null) {
                        options.getCallback().callback(holder.getResponse());
                    }
                }
            }, options.isAsync());
        } else {
            if (options.getDeliveryPolicy() == MessageOptions.DeliveryPolicy.THROW) {
                throw new NoSubscriptionMessageException(String.format("No subscribers for message %s", topic));
            }
        }
    }
    
    /**
     * Get subscription for message id;
     * @param messageId id of message to address;
     * @return subscription holder;
     */
    private static Subscription getSubscription(final String messageId) {
        for (Subscription subscription: subscriptions) {
            if (Objects.equals(subscription.getTopic(), messageId)) {
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
        return !result.containsKey(GlobalCons.G_EXCEPTION_HEADER);
    }
    
    /**
     * Init message bus. Need to call just once.
     */
    public static void init() {
        init.ensureInit();
    }
}
