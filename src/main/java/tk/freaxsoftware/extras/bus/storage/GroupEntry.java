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

/**
 * Message grouping config entry.
 * @author Stanislav Nepochatov
 * @since 5.0
 */
public class GroupEntry {
    
    /**
     * Topic to send single instances (for grouping).
     */
    private String topicSingle;
    
    /**
     * Topic to send grouped list of messages.
     */
    private String topicList;
    
    /**
     * Max size of the grouped message list to triger sending.
     */
    private Integer maxSize;
    
    /**
     * Max time for first message to be present in queue before trigger sending.
     */
    private Integer maxTimeInQueue;

    public String getTopicSingle() {
        return topicSingle;
    }

    public void setTopicSingle(String topicSingle) {
        this.topicSingle = topicSingle;
    }

    public String getTopicList() {
        return topicList;
    }

    public void setTopicList(String topicList) {
        this.topicList = topicList;
    }

    public Integer getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(Integer maxSize) {
        this.maxSize = maxSize;
    }

    public Integer getMaxTimeInQueue() {
        return maxTimeInQueue;
    }

    public void setMaxTimeInQueue(Integer maxTimeInQueue) {
        this.maxTimeInQueue = maxTimeInQueue;
    }
    
}
