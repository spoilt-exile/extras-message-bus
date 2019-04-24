/*
 * This file is part of MessageBus library.
 * 
 * Copyright (C) 2018 Freax Software
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
package tk.freaxsoftware.extras.bus.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processing message config by using system properties.
 * @author Stanislav Nepochatov
 */
public class PropertyConfigProcessor {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyConfigProcessor.class);
    
    public enum Properties {
        
        /**
         * Set heartbeat rate for bridge server.
         */
        BRIDGE_SERVER_HEART_BEAT_RATE("bridge.server.hearbeat", (config, property) -> {config.getBridgeServer().setHeartbeatRate(getPropertyAsInt(property));}),
        
        /**
         * Set http port for server.
         */
        BRIDGE_SERVER_HTTP_PORT("bridge.server.port", (config, property) -> {config.getBridgeServer().setHttpPort(getPropertyAsInt(property));}),
        
        /**
         * Set address for server to connect.
         */
        BRIDGE_CLIENT_ADDRESS("bridge.client.address", (config, property) -> {config.getBridgeClient().setAddress(property);}),
        
        /**
         * Set http port for client to comminicate with server.
         */
        BRIDGE_CLIENT_PORT("bridge.client.port", (config, property) -> {config.getBridgeClient().setPort(getPropertyAsInt(property));});
        
        private final String propertyId;

        private final PropertyProcessor processor;

        private Properties(String propertyId, PropertyProcessor processor) {
            this.propertyId = propertyId;
            this.processor = processor;
        }

        public String getPropertyId() {
            return propertyId;
        }

        public PropertyProcessor getProcessor() {
            return processor;
        }
        
        public static Integer getPropertyAsInt(String property) {
            return Integer.getInteger(property);
        }
    }
    
    public interface PropertyProcessor {
        void processOverride(MessageBusConfig config, String property);
    }

    /**
     * Process config and set overrided by properties values to it.
     * @param config message bus config;
     */
    public static void process(MessageBusConfig config) {
        for (Properties overProperty: Properties.values()) {
            if (isPropertyAvailable(overProperty.getPropertyId())) {
                try {
                    LOGGER.info("Overriding value by system property {}", overProperty.getPropertyId());
                    overProperty.getProcessor().processOverride(config, overProperty.getPropertyId());
                } catch (Exception ex) {
                    LOGGER.error("Unable to override property {} by value {}", overProperty.getPropertyId(), System.getProperty(overProperty.getPropertyId()), ex);
                }
            }
        }
    }
    
    private static Boolean isPropertyAvailable(String property) {
        return System.getProperties().containsKey(property);
    }
    
}
