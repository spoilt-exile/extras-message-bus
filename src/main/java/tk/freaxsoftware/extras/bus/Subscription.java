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

import java.util.ArrayList;
import java.util.List;

/**
 * Message listener holder for specified message id.
 * @author Stanislav Nepochatov
 */
public class Subscription {
    
    /**
     * Id of message subscription.
     */
    private final String id;
    
    /**
     * Receivers list.
     */
    private List<Receiver> receivers;
    
    /**
     * Default constructor.
     * @param id id of subscription.
     */
    public Subscription(String id) {
        this.id = id;
        receivers = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public List<Receiver> getReceivers() {
        return receivers;
    }

    public void setReceivers(List<Receiver> receivers) {
        this.receivers = receivers;
    }
    
    public void addReceiver(Receiver receiver) {
        this.receivers.add(receiver);
    }
}
