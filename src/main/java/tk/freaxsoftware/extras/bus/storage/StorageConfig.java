/*
 * This file is part of MessageBus library.
 * 
 * Copyright (C) 2020 Freax Software
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
package tk.freaxsoftware.extras.bus.storage;

import java.util.Map;
import tk.freaxsoftware.extras.bus.MessageOptions;

/**
 * Storage config class.
 * @author Stanislav Nepochatov
 * @since 5.0
 */
public class StorageConfig {
    
    /**
     * Full path for class which implements MessageStorage.
     * @see MessageStorage
     */
    private String storageClass;
    
    /**
     * Key-value arguments to init storage implementation.
     */
    private Map<String, String> storageClassArgs;
    
    /**
     * Redelivery period for messages in seconds.
     */
    private Integer redeliveryPeriod;
    
    /**
     * Topic pattern for matched messages to be stored.
     */
    private String topicPattern;
    
    /**
     * Store messages with CALL delivery policy.
     * @see MessageOptions.DeliveryPolicy#CALL
     */
    private Boolean storeCalls;
    
    /**
     * Remove processed messages from storage.
     */
    private Boolean removeProcessed;

    public String getStorageClass() {
        return storageClass;
    }

    public void setStorageClass(String storageClass) {
        this.storageClass = storageClass;
    }

    public Map<String, String> getStorageClassArgs() {
        return storageClassArgs;
    }

    public void setStorageClassArgs(Map<String, String> storageClassArgs) {
        this.storageClassArgs = storageClassArgs;
    }

    public Integer getRedeliveryPeriod() {
        return redeliveryPeriod;
    }

    public void setRedeliveryPeriod(Integer redeliveryPeriod) {
        this.redeliveryPeriod = redeliveryPeriod;
    }

    public String getTopicPattern() {
        return topicPattern;
    }

    public void setTopicPattern(String topicPattern) {
        this.topicPattern = topicPattern;
    }

    public Boolean getStoreCalls() {
        return storeCalls;
    }

    public void setStoreCalls(Boolean storeCalls) {
        this.storeCalls = storeCalls;
    }

    public Boolean getRemoveProcessed() {
        return removeProcessed;
    }

    public void setRemoveProcessed(Boolean removeProcessed) {
        this.removeProcessed = removeProcessed;
    }
    
    public boolean isValid() {
        return (storageClass != null && !storageClass.isBlank()) && redeliveryPeriod != null 
                && (topicPattern != null && !topicPattern.isBlank()) 
                && storeCalls != null && removeProcessed != null;
    }
   
}
