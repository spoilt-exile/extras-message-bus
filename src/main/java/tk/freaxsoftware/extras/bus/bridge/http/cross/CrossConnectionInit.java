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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.freaxsoftware.extras.bus.MessageBus;
import tk.freaxsoftware.extras.bus.MessageHolder;
import tk.freaxsoftware.extras.bus.Receiver;

/**
 * Cross conecction init for clients.
 * @author Stanislav Nepochatov
 * @since 5.0
 */
public class CrossConnectionInit implements Receiver<CrossNode> {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(CrossConnectionInit.class);
    
    private final String[] demandsTopics;

    public CrossConnectionInit(String[] demandsTopics) {
        this.demandsTopics = demandsTopics;
    }

    @Override
    public void receive(MessageHolder<CrossNode> message) throws Exception {
        CrossNode node = message.getContent();
        String[] crossConnectionTopics = findCrossConnections(node.getOfferTopics());
        if (crossConnectionTopics.length > 0) {
            LOGGER.warn("Init cross connection to node {} port {} with topic subscriptions {}.", 
                    node.getNodeIp(), node.getNodePort(), crossConnectionTopics);
            CrossConnectionSender sender = new CrossConnectionSender(node.getNodeIp(), node.getNodePort());
            MessageBus.addSubscriptions(crossConnectionTopics, sender);
            sender.addSubscriptions(new HashSet(Arrays.asList(crossConnectionTopics)));
        }
    }
    
    private String[] findCrossConnections(String[] nodeOfferTopics) {
        Set<String> cross = new HashSet<>();
        for (String demand: demandsTopics) {
            for (String offer: nodeOfferTopics) {
                if (Objects.equals(offer, demand)) {
                    cross.add(offer);
                }
            }
        }
        return cross.toArray(new String[cross.size()]);
    }
}
