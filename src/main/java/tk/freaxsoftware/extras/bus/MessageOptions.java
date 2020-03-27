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

/**
 * Message options for optional
 * @author Stanislav Nepochatov
 */
public class MessageOptions {
    
    /**
     * Async flag of message. If activated all message processing 
     * will be done in separate thread (across all receivers).
     */
    private boolean async;
    
    /**
     * Broadcast flag of message. If activated message will be 
     * delivered to all receivers. If disactivated then receiver will 
     * be choosen with round-robin strategy.
     */
    private boolean broadcast;
    
    /**
     * Message optional headers. Useful to deliver secondary data. 
     * Accepts only strings.
     */
    private Map<String, String> headers;
    
    /**
     * Describes behavior of the bus if message is unprocessed.
     */
    private DeliveryPolicy deliveryPolicy;
    
    /**
     * Coutner for redelivery attemtps.
     */
    private Integer redeliveryCounter = 3;
    
    /**
     * Callback for processing of message results. Can't be used 
     * with conjunction with braodcast messages.
     */
    private Callback callback;

    /**
     * Private constructor.
     * @param async async flag;
     * @param broadcast broadcast flag;
     * @param deliveryPolicy delivery policy;
     * @param callback callback handler;
     * @param headers message headers;
     */
    private MessageOptions(boolean async, boolean broadcast, 
            DeliveryPolicy deliveryPolicy, Callback callback, 
            Map<String, String> headers) {
        this.async = async;
        this.broadcast = broadcast;
        this.deliveryPolicy = deliveryPolicy;
        this.callback = callback;
        this.headers = headers != null ? headers : new HashMap<>();
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public boolean isBroadcast() {
        return broadcast;
    }

    public void setBroadcast(boolean broadcast) {
        this.broadcast = broadcast;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public DeliveryPolicy getDeliveryPolicy() {
        return deliveryPolicy;
    }

    public void setDeliveryPolicy(DeliveryPolicy deliveryPolicy) {
        this.deliveryPolicy = deliveryPolicy;
    }

    public Integer getRedeliveryCounter() {
        return redeliveryCounter;
    }

    public void setRedeliveryCounter(Integer redeliveryCounter) {
        this.redeliveryCounter = redeliveryCounter;
    }

    public Callback getCallback() {
        return callback;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }
    
    /**
     * Delivery policy for unprocessed messages.
     */
    public static enum DeliveryPolicy {
        
        /**
         * Forget about unprocessed messages.
         */
        VOID,
        
        /**
         * Throw an exception if no subscribers available.
         */
        CALL,
        
        /**
         * Store messages in storage until subscriber unavailable.
         */
        STORE;
    }
    
    /**
     * Builds defatul message options: sync point-to-point message without callback.
     * @param headers message headers;
     * @return message options instance;
     */
    public static MessageOptions defaultOptions(Map<String, String> headers) {
        return new MessageOptions(false, false, DeliveryPolicy.VOID, null, headers);
    }
    
    /**
     * Builds message options for notifications: async, broadcast, with storage for unprocessed messages.
     * @param headers message headers;
     * @return message options instance;
     */
    public static MessageOptions notificationOptions(Map<String, String> headers) {
        return new MessageOptions(true, true, DeliveryPolicy.STORE, null, headers);
    }
    
    /**
     * Builds message options for call to another system: sync, point-to-point, ensured with callback.
     * @param headers message headers;
     * @param callback message callback;
     * @return message options instance;
     */
    public static MessageOptions callOptions(Map<String, String> headers, Callback callback) {
        return new MessageOptions(false, false, DeliveryPolicy.CALL, callback, headers);
    }
    
    /**
     * Builder for message properties.
     */
    public static class Builder {
        
        /**
         * Options instance.
         */
        private final MessageOptions instance;
        
        /**
         * Default constructor. Will build default options.
         */
        private Builder() {
            instance = MessageOptions.defaultOptions(null);
        }
        
        /**
         * Return new builder.
         * @return builder;
         */
        public static Builder newInstance() {
            return new Builder();
        }
        
        /**
         * Set sync mode for message.
         * @return builder instance;
         */
        public Builder sync() {
            this.instance.setAsync(false);
            return this;
        }
        
        /**
         * Set async mode for message.
         * @return builder instance;
         */
        public Builder async() {
            this.instance.setAsync(true);
            return this;
        }
        
        /**
         * Set point-to-point mode for message.
         * @return builder instance;
         */
        public Builder pointToPoint() {
            this.instance.setBroadcast(false);
            return this;
        }
        
        /**
         * Set broadcast mode for message. 
         * May throw {@code IllegalStateException} if callback already present in builder.
         * @return builder instance;
         */
        public Builder broadcast() {
            if (this.instance.getCallback() != null) {
                throw new IllegalStateException("Can't broadcast message with callback!");
            }
            this.instance.setBroadcast(true);
            return this;
        }
        
        /**
         * Put header in the message.
         * @param name name of header;
         * @param value value of header;
         * @return builder instance;
         */
        public Builder header(String name, String value) {
            this.instance.getHeaders().put(name, value);
            return this;
        }
        
        /**
         * Put header in the message.
         * @param headerMap map of headers;
         * @return builder instance;
         */
        public Builder headers(Map<String, String> headerMap) {
            this.instance.getHeaders().putAll(headerMap);
            return this;
        }
       
        /**
         * Use VOID delivery policy. Acceptable only for messages with minor importance.
         * @return builder instance.
         */
        public Builder deliveryVoid() {
            this.instance.setDeliveryPolicy(DeliveryPolicy.VOID);
            return this;
        }
        
        /**
         * Use CALL delivery policy. Fit for timed operations where response required.
         * @return builder instance.
         */
        public Builder deliveryCall() {
            this.instance.setDeliveryPolicy(DeliveryPolicy.CALL);
            return this;
        }
        
        /**
         * Use STORE delivery policy. Fit for messages which can be processed anytime in future.
         * @return builder instance.
         */
        public Builder deliveryNotification() {
            this.instance.setDeliveryPolicy(DeliveryPolicy.STORE);
            return this;
        }
        
        /**
         * Use STORE delivery policy. Fit for messages which can be processed anytime in future.
         * @param redeliveryCounter number of attempts for redelivery before droping message;
         * @return builder instance.
         */
        public Builder deliveryNotification(Integer redeliveryCounter) {
            this.instance.setDeliveryPolicy(DeliveryPolicy.STORE);
            this.instance.setRedeliveryCounter(redeliveryCounter);
            return this;
        }
        
        /**
         * Set callback handler. It will be executed after all receivers execution. 
         * May throw {@code IllegalStateException} if broadcast mode already set in builder.
         * @param callback callback handler instance;
         * @return builder instance;
         */
        public Builder callback(Callback callback) {
            if (this.instance.isBroadcast()) {
                throw new IllegalStateException("Can't broadcast message with callback!");
            }
            this.instance.setCallback(callback);
            return this;
        }
        
        /**
         * Return message options from builder.
         * @return message options;
         */
        public MessageOptions build() {
            return this.instance;
        }
    }
    
}
