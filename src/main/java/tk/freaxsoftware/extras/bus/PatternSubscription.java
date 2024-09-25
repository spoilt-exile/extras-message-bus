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
package tk.freaxsoftware.extras.bus;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Message listener for specified message topic pattern.
 * @author Stanislav Nepochatov
 */
public class PatternSubscription {
    
    /**
     * Topic pattern of message subscription.
     */
    private final String pattern;
    
    /**
     * Receivers list.
     */
    private List<Receiver> receivers;

    public PatternSubscription(String pattern) {
        this.pattern = pattern;
        receivers = new CopyOnWriteArrayList<>();
    }

    public String getPattern() {
        return pattern;
    }

    public List<Receiver> getReceivers() {
        return receivers;
    }

    public void setReceivers(List<Receiver> receivers) {
        this.receivers = receivers;
    }
    
    /**
     * Add specified receiver to queue.
     * @param receiver receiver to add;
     */
    public void addReceiver(Receiver receiver) {
        this.receivers.add(receiver);
    }
    
    public void removeReceiver(Receiver receiver) {
        this.receivers.remove(receiver);
    }
    
    public boolean isMatched(String topic) {
        return topic.matches(pattern);
    } 
}
