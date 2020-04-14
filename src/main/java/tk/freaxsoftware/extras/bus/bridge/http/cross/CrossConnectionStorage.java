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

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.freaxsoftware.extras.bus.MessageBus;
import tk.freaxsoftware.extras.bus.MessageHolder;
import tk.freaxsoftware.extras.bus.MessageOptions;
import tk.freaxsoftware.extras.bus.Receiver;
import tk.freaxsoftware.extras.bus.bridge.http.LocalHttpCons;

/**
 * Storage for cross connection data on central node;
 * @author Stanislav Nepochatov
 * @since 5.0
 */
public class CrossConnectionStorage implements Receiver<CrossNode> {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(CrossConnectionStorage.class);
    
    private Set<CrossNode> nodes = new HashSet();

    @Override
    public void receive(MessageHolder<CrossNode> message) throws Exception {
        CrossNode newNode = message.getContent();
        newNode.setNodeIp((String) message.getHeaders().get(LocalHttpCons.L_HTTP_NODE_IP_HEADER));
        LOGGER.info("Processing node {} port {};", newNode.getNodeIp(), newNode.getNodePort());
        for (CrossNode node: nodes) {
            if (isCrossConnection(newNode.getOfferTopics(), node.getDemandTopics())) {
                MessageBus.fire(String.format(LocalHttpCons.L_HTTP_CROSS_NODE_UP_TOPIC_FORMAT, 
                        node.getNodeIp(), node.getNodePort()), newNode, 
                        MessageOptions.Builder.newInstance().async().pointToPoint().build());
            }
            if (isCrossConnection(newNode.getDemandTopics(), node.getOfferTopics())) {
                MessageBus.fire(String.format(LocalHttpCons.L_HTTP_CROSS_NODE_UP_TOPIC_FORMAT, 
                        newNode.getNodeIp(), newNode.getNodePort()), node, 
                        MessageOptions.Builder.newInstance().async().pointToPoint().build());
            }
        }
        nodes.add(newNode);
    }
    
    private Boolean isCrossConnection(String[] offers, String[] demands) {
        for (String offer: offers) {
            for (String demand: demands) {
                if (Objects.equals(offer, demand)) {
                    return true;
                }
            }
        }
        return false;
    }
    
}
