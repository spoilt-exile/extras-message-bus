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
     * Callback for processing of message results. Can't be used 
     * with conjunction with braodcast messages.
     */
    private Callback callback;

    /**
     * Private constructor.
     * @param async async flag;
     * @param broadcast broadcast flag;
     * @param callback callback handler;
     */
    private MessageOptions(boolean async, boolean broadcast, Callback callback) {
        this.async = async;
        this.broadcast = broadcast;
        this.callback = callback;
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

    public Callback getCallback() {
        return callback;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }
    
    /**
     * Builds defatul message options: sync point-to-point message without callback.
     * @return message options instance;
     */
    public static MessageOptions defaultOptions() {
        return new MessageOptions(false, false, null);
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
            instance = MessageOptions.defaultOptions();
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
