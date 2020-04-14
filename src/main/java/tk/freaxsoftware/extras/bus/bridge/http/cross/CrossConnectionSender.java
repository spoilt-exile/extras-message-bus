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
package tk.freaxsoftware.extras.bus.bridge.http.cross;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.freaxsoftware.extras.bus.MessageBus;
import tk.freaxsoftware.extras.bus.MessageHolder;
import tk.freaxsoftware.extras.bus.bridge.http.MessagePeerSender;

/**
 * Cross connection sender for nodes.
 * @author Stanislav Nepochatov
 * @since 5.0
 */
public class CrossConnectionSender extends MessagePeerSender {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(CrossConnectionSender.class);
    
    private Integer breakCounter = 3;

    public CrossConnectionSender(String address, Integer port) {
        super(address, port);
    }

    @Override
    public void receive(MessageHolder message) throws Exception {
        try {
            super.receive(message);
            breakCounter = 3;
        } catch (Exception ex) {
            LOGGER.error("Cross connection node {} port {} thrown exception {}, breakCounter = {}", 
                    this.address, this.port, ex.getClass(), breakCounter);
            breakCounter--;
            if (breakCounter == 0) {
                LOGGER.error("Terminating cross connection node {} port {}", this.address, this.port);
                for (String subscruptionTopic: this.subscriptions) {
                    MessageBus.removeSubscription(subscruptionTopic, this);
                }
            }
        }
    }
    
    
    
}
