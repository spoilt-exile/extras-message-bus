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
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static tk.freaxsoftware.extras.bus.bridge.http.LocalHttpCons.L_HTTP_HEARTBEAT_TOPIC;
import tk.freaxsoftware.extras.bus.exceptions.ExceptionServices;
import tk.freaxsoftware.extras.bus.exceptions.NoSubscriptionMessageException;
import tk.freaxsoftware.extras.bus.exceptions.ReceiverRegistrationException;
import tk.freaxsoftware.extras.bus.executor.MessageExecutorFactory;

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
     * List of pattern match subscriptions for messages.
     */
    private static final List<PatternSubscription> patternSubscriptions = new CopyOnWriteArrayList<>();
    
    /**
     * Message bus init util.
     */
    private static MessageBusInit init = new MessageBusInit();
    
    /**
     * Set of topic names to filter out from logging.
     */
    private static final Set<String> filterLogTopics = Set.of(L_HTTP_HEARTBEAT_TOPIC);
    
    /**
     * Subscribe receiver for message with following topic.
     * @param topic message topic destination. If topic contains any symbol beside letters, digits and dot it will be handled as pattern subscription;
     * @param receiver message receiver;
     */
    public static void addSubscription(final String topic, final Receiver receiver) {
        init();
        if (topic == null || receiver == null) {
            throw new ReceiverRegistrationException("Can't processed registration with null references!");
        }
        boolean isPattern = !topic.matches("^[a-zA-Z0-9.]+$");
        LOGGER.info("Add new subscription for {} is pattern {}", topic, isPattern);
        if (isPattern) {
            PatternSubscription patternSubscription = getPatternSubscription(topic);
            if (patternSubscription == null) {
                patternSubscription = new PatternSubscription(topic);
                patternSubscription.addReceiver(receiver);
                patternSubscriptions.add(patternSubscription);
            } else {
                patternSubscription.addReceiver(receiver);
            }
        } else {
            Subscription subscription = getSubscription(topic);
            if (subscription == null) {
                subscription = new Subscription(topic);
                subscription.addReceiver(receiver);
                subscriptions.add(subscription);
            } else {
                subscription.addReceiver(receiver);
            }
        }
        MessageBus.fire(GlobalCons.G_SUBSCRIBE_TOPIC, receiver, 
                MessageOptions.Builder.newInstance().async().broadcast()
                        .header(GlobalCons.G_SUBSCRIPTION_DEST_HEADER, topic).build());
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
     * Tests if there is subscription for specified topic.
     * @param topic topic to search;
     * @return true if there is valid subsciription for topic / false if there is none;
     */
    public static boolean isSubscribed(final String topic) {
        return getSubscription(topic) != null || getPatternSubscription(topic) != null;
    }
    
    /**
     * Unsubscribe following receiver from message topic.
     * @param topic message topic destination. If topic contains any symbol beside letters, digits and dot it will be handled as pattern subscription;
     * @param receiver the same receiver instance which were using during subscription;
     */
    public static void removeSubscription(final String topic, final Receiver receiver) {
        init();
        boolean isPattern = !topic.matches("^[a-zA-Z0-9.]+$");
        LOGGER.info("Removing subscription for {} is patter {}", topic, isPattern);
        if (isPattern) {
            PatternSubscription patternSubscription = getPatternSubscription(topic);
            if (patternSubscription != null) {
                patternSubscription.getReceivers().remove(receiver);
                if (patternSubscription.getReceivers().isEmpty()) {
                    subscriptions.remove(patternSubscription);
                }
            }
        } else {
            Subscription subscription = getSubscription(topic);
            if (subscription != null) {
                subscription.getReceivers().remove(receiver);
                if (subscription.getReceivers().isEmpty()) {
                    subscriptions.remove(subscription);
                }
            }
        }
        MessageBus.fire(GlobalCons.G_UNSUBSCRIBE_TOPIC, receiver, 
                MessageOptions.Builder.newInstance().async().broadcast()
                        .header(GlobalCons.G_SUBSCRIPTION_DEST_HEADER, topic).build());
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
     * @deprecated 
     */
    public static void clearBus() {
        //init = new MessageBusInit();
        //init.ensureInit();
        //LOGGER.info("clearing all subscriptions... Reinit subscriptions to proceed.");
        //subscriptions.clear();
    }
    
    /**
     * Fire message to the bus.
     * @param <T> type of content;
     * @param topic destination of message;
     * @param content message content;
     */
    public static <T> void fire(final String topic, final T content) {
        MessageBus.fire(topic, content, MessageOptions.defaultOptions(null));
    }
    
    /**
     * Fire message to the bus.
     * @param <T> type of content;
     * @param topic destination of message;
     * @param options options for message processing;
     */
    public static <T> void fire(final String topic, final MessageOptions options) {
        MessageBus.fire(topic, null, options);
    }
    
    /**
     * Fire message to the bus.
     * @param <T> type of content;
     * @param topic destination of message;
     * @param content message content;
     * @param options options for message processing;
     */
    public static <T> void fire(final String topic, final T content, final MessageOptions options) {
        MessageHolder<T> holder = new MessageHolder<>(topic, options, content);
        if (!filterLogTopics.contains(topic)) {
            LOGGER.info("Message with topic {} fired to bus", topic);
        }
        fire(holder);
    }
    
    /**
     * Fire method to submit message holder to bus.
     * @param holder message holder to process; 
     */
    public static void fire(MessageHolder holder) {
        init();
        if (holder.getOptions() == null) {
            throw new IllegalArgumentException("Message options can't be null!");
        }
        Subscription subscription = getSubscription(holder.getTopic());
        holder.setStatus(MessageStatus.PROCESSING);
        init.getExecutor().execute(
                MessageExecutorFactory.newExecutor(holder, subscription, init), holder.getOptions().isAsync()
        );
        processPatternSubscriptions(holder);
    }
    
    /**
     * Fire sync call. Used for direct sync call only. Allows to get response content without callback.
     * @param <T> type of message content;
     * @param <R> type of the response content;
     * @param topic destination of message;
     * @param content message content;
     * @param options options for message processing;
     * @param responseClass class to cast response;
     * @return content of message response;
     * @throws Exception exception on message processing;
     */
    public static <T, R> R fireCall(final String topic, final T content, final MessageOptions options, final Class<R> responseClass) throws Exception {
        init();
        options.setAsync(false);
        options.setBroadcast(false);
        options.setDeliveryPolicy(MessageOptions.DeliveryPolicy.CALL);
        if (options == null) {
            throw new IllegalArgumentException("Message options can't be null!");
        }
        MessageHolder<T> holder = new MessageHolder<>(topic, options, content);
        MessageContextHolder.setContext(new MessageContext(holder.getTrxId()));
        Subscription subscription = getSubscription(holder.getTopic());
        init.getInterceptor().storeMessage(holder);
        if (subscription != null) {
            Integer tryIndex = 0;
            while (tryIndex < holder.getOptions().getRedeliveryCounter()) {
                Receiver rc = subscription.getRoundRobinIterator().next();
                rc.receive(holder);
                holder.setStatus(MessageStatus.FINISHED);
                init.getInterceptor().storeProcessedMessage(holder);
                ExceptionServices.callback(holder.getResponse());
                break;
            }
            processPatternSubscriptions(holder);
            if (holder.getStatus() != MessageStatus.FINISHED) {
                LOGGER.warn("Message {} on topic {} exhaust redelivery attempts, dropping.", 
                        holder.getId(), holder.getTopic());
            }
            return (R) holder.getResponse().getContent();
        } else {
            holder.setStatus(MessageStatus.ERROR);
            init.getInterceptor().storeMessage(holder);
            throw new NoSubscriptionMessageException(String.format("No subscribers for message %s", holder.getTopic()));
        }
    }
    
    /**
     * Process message for pattern matching receivers.
     * @param holder message holder;
     */
    private static void processPatternSubscriptions(MessageHolder holder) {
        final Set<Receiver> patterReceivers = getPatternSubscriptionReceivers(holder.getTopic());
        init.getExecutor().executeAsync(() -> {
            patterReceivers.forEach(rec -> {
                try {
                    rec.receive(holder);
                } catch (Exception ex) {
                    LOGGER.error("Receiver " + rec.getClass().getName() + " for topic " + holder.getTopic() + " throws exception", ex);
                }
            });
        });
    }
    
    /**
     * Get subscription for message topic;
     * @param topic topic of message to address;
     * @return subscription holder;
     */
    private static Subscription getSubscription(final String topic) {
        for (Subscription subscription: subscriptions) {
            if (Objects.equals(subscription.getTopic(), topic)) {
                return subscription;
            }
        }
        return null;
    }
    
    /**
     * Get pattern subscription for message topic without pattern matching;
     * @param topicPattern topic pattern of message to address;
     * @return subscription holder;
     */
    private static PatternSubscription getPatternSubscription(final String topicPattern) {
        for (PatternSubscription subscription: patternSubscriptions) {
            if (Objects.equals(subscription.getPattern(), topicPattern)) {
                return subscription;
            }
        }
        return null;
    }
    
    /**
     * Get pattern subscription for message topic;
     * @param topic topic of message to address;
     * @return subscription holder;
     */
    private static Set<Receiver> getPatternSubscriptionReceivers(final String topic) {
        return patternSubscriptions.stream()
                .filter(sub -> sub.isMatched(topic))
                .flatMap(sub -> sub.getReceivers().stream())
                .collect(Collectors.toSet());
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
        init.ensureInit("bus.json");
    }
    
    /**
     * Init message bus with custom config.Need to call just once.
     * @param configFileName filename of the config to load;
     */
    public static void init(String configFileName) {
        init.ensureInit(configFileName);
    }
}
