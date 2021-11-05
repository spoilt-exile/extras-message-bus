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
import tk.freaxsoftware.extras.bus.annotation.Receive;
import tk.freaxsoftware.extras.bus.bridge.http.LocalHttpCons;

/**
 * Cross conecction init for clients.
 * @author Stanislav Nepochatov
 * @since 5.0
 */
public class CrossConnectionInit {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(CrossConnectionInit.class);
    
    private final String[] sendsTopics;
    
    private final Set<CrossConnectionSender> senders = new HashSet();

    public CrossConnectionInit(String[] sendsTopics) {
        this.sendsTopics = sendsTopics;
    }

    @Receive(value = {LocalHttpCons.L_HTTP_CROSS_NODE_UP_TOPIC})
    public void crossNodeUp(MessageHolder<CrossNode> message) throws Exception {
        CrossNode node = message.getContent();
        String[] crossConnectionTopics = findCrossConnections(node.getReceiveTopics());
        if (crossConnectionTopics.length > 0) {
            LOGGER.warn("Init cross connection to node {} port {} with topic to send {}.", 
                    node.getNodeIp(), node.getNodePort(), crossConnectionTopics);
            CrossConnectionSender sender = new CrossConnectionSender(node.getNodeIp(), node.getNodePort());
            MessageBus.addSubscriptions(crossConnectionTopics, sender);
            sender.addSubscriptions(new HashSet(Arrays.asList(crossConnectionTopics)));
            senders.add(sender);
        }
    }
    
    @Receive(value = {LocalHttpCons.L_HTTP_CROSS_NODE_DOWN_TOPIC})
    public void crossNodeDown(MessageHolder<CrossNode> message) {
        CrossNode node = message.getContent();
        CrossConnectionSender senderToDelete = null;
        for (CrossConnectionSender crossSender: senders) {
            if (Objects.equals(crossSender.getAddress(), node.getNodeIp()) 
                    && Objects.equals(crossSender.getPort(), node.getNodePort())) {
                LOGGER.warn("Closing cross connection to node {} port {} with topics {}.",
                        crossSender.getAddress(), crossSender.getPort(), crossSender.getSubscriptions());
                MessageBus.removeSubscriptions(crossSender.getSubscriptions(), crossSender);
                senderToDelete = crossSender;
                break;
            }
        }
        if (senderToDelete != null) {
            senders.remove(senderToDelete);
        }
    }
    
    private String[] findCrossConnections(String[] nodeReceiveTopics) {
        Set<String> cross = new HashSet<>();
        for (String send: sendsTopics) {
            for (String receive: nodeReceiveTopics) {
                if (Objects.equals(receive, send)) {
                    cross.add(receive);
                }
            }
        }
        return cross.toArray(new String[cross.size()]);
    }
}
