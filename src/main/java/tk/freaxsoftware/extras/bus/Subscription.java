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
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Message listener holder for specified message id.
 * @author Stanislav Nepochatov
 */
public class Subscription {
    
    /**
     * Topic of message subscription.
     */
    private final String topic;
    
    /**
     * Receivers list.
     */
    private List<Receiver> receivers;
    
    /**
     * Instance of the round robin iterator.
     */
    private RoundRobinIterator<Receiver> roundRobinIterator;
    
    /**
     * Default constructor.
     * @param topic destination of subscription.
     */
    public Subscription(String topic) {
        this.topic = topic;
        receivers = new CopyOnWriteArrayList<>();
    }

    public String getTopic() {
        return topic;
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

    public RoundRobinIterator<Receiver> getRoundRobinIterator() {
        if (roundRobinIterator == null) {
            roundRobinIterator = new RoundRobinIterator<>(receivers);
        }
        return roundRobinIterator;
    }
}
