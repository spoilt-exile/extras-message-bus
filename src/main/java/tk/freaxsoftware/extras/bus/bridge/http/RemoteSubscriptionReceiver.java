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

package tk.freaxsoftware.extras.bus.bridge.http;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.freaxsoftware.extras.bus.GlobalCons;
import tk.freaxsoftware.extras.bus.MessageBus;
import tk.freaxsoftware.extras.bus.MessageHolder;
import tk.freaxsoftware.extras.bus.Receiver;

/**
 * Remote subscription manager. Listens for remove subscribe/unsubscribe messages and creates 
 * {@code MessagePeerSender} for each node.
 * @author Stanislav Nepochatov
 */
public class RemoteSubscriptionReceiver implements Receiver {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteSubscriptionReceiver.class);
    
    /**
     * Remote subscribers map: node ip over senders.
     */
    private final Map<String, MessagePeerSender> senderMap;
    
    private ExecutorService threadService = Executors.newSingleThreadExecutor();

    public RemoteSubscriptionReceiver() {
        senderMap = new ConcurrentHashMap<>();
    }
    
    public RemoteSubscriptionReceiver(Integer heartBeatMaxAge) {
        this();
        if (heartBeatMaxAge > 0) {
            LOGGER.info(String.format("Creating remote receiver with heartbeat: %d", heartBeatMaxAge));
            threadService.submit(new Runnable() {

                public void run() {
                    while (true) {
                        for (Entry<String, MessagePeerSender> senderEntry: senderMap.entrySet()) {
                            if (senderEntry.getValue().isBeatExpired(heartBeatMaxAge)) {
                                LOGGER.warn(String.format("Killing node %s cause expired heartbeat.", senderEntry.getKey()));
                                MessageBus.removeSubscriptions(senderEntry.getValue().getSubscriptions(), senderEntry.getValue());
                                MessageBus.removeSubscription(String.format(LocalHttpCons.L_HTTP_CROSS_NODE_UP_TOPIC_FORMAT, 
                                    senderEntry.getValue().getAddress(), senderEntry.getValue().getPort()), senderEntry.getValue());
                                senderMap.remove(senderEntry.getKey());
                            }
                        }
                        try {
                            Thread.sleep(heartBeatMaxAge * 1000);
                        } catch (InterruptedException ex) {
                            LOGGER.error("Killer thread interrupted!", ex);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void receive(MessageHolder message) throws Exception {
        String subscriptionId = (String) message.getHeaders().get(GlobalCons.G_SUBSCRIPTION_DEST_HEADER);
        String nodeIp = (String) message.getHeaders().get(LocalHttpCons.L_HTTP_NODE_IP_HEADER);
        Integer nodePort = Integer.parseInt((String) message.getHeaders().get(LocalHttpCons.L_HTTP_NODE_PORT_HEADER));
        String nodeKey = nodeIp + ":" + nodePort;
        if (!Objects.equals(message.getTopic(), LocalHttpCons.L_HTTP_HEARTBEAT_TOPIC)) {
            LOGGER.info(String.format("Getting message %s from node %s on port %d", message.getTopic(), nodeIp, nodePort));
        }
        switch (message.getTopic()) {
            case LocalHttpCons.L_HTTP_SUBSCRIBE_TOPIC:
                MessagePeerSender peerSender;
                if (!senderMap.containsKey(nodeKey)) {
                    LOGGER.info("Creating new subscriber for node.");
                    peerSender = new MessagePeerSender(nodeIp, nodePort);
                    senderMap.put(nodeKey, peerSender);
                    String localNodeUp = String.format(LocalHttpCons.L_HTTP_CROSS_NODE_UP_TOPIC_FORMAT, 
                                    nodeIp, nodePort);
                    MessageBus.addSubscription(localNodeUp, peerSender);
                    peerSender.addSubscription(localNodeUp);
                } else {
                    peerSender = senderMap.get(nodeKey);
                }
                peerSender.addSubscription(subscriptionId);
                MessageBus.addSubscription(subscriptionId, peerSender);
                break;
            case LocalHttpCons.L_HTTP_UNSUBSCRIBE_TOPIC:
                if (senderMap.containsKey(nodeKey)) {
                    MessagePeerSender peerSender2 = senderMap.get(nodeKey);
                    peerSender2.removeSubscription(subscriptionId);
                    MessageBus.removeSubscription(subscriptionId, peerSender2);
                    //TO-DO: add proper logic to exclude node up
                    if (peerSender2.isEmpty()) {
                        LOGGER.info("Removing subscriber for node.");
                        senderMap.remove(nodeKey);
                        MessageBus.removeSubscription(String.format(LocalHttpCons.L_HTTP_CROSS_NODE_UP_TOPIC_FORMAT, 
                                    nodeIp, nodePort), peerSender2);
                    }
                }
                break;
            case LocalHttpCons.L_HTTP_HEARTBEAT_TOPIC:
                if (senderMap.containsKey(nodeKey)) {
                    MessagePeerSender peerSender3 = senderMap.get(nodeKey);
                    peerSender3.beat();
                } else {
                    LOGGER.info("Reinit connection for " + nodeIp + " on port " + nodePort);
                    MessagePeerSender peerSender4 = new MessagePeerSender(nodeIp, nodePort);
                    senderMap.put(nodeKey, peerSender4);
                    if (message.getContent() != null) {
                        Set<String> reconnectIds = (Set) message.getContent();
                        MessageBus.addSubscriptions(reconnectIds.toArray(new String[reconnectIds.size()]), peerSender4);
                        peerSender4.addSubscriptions(reconnectIds);
                    } else {
                        LOGGER.error("Can't reinit connection for " + nodeIp + " on port " + nodePort + " due to lack of list of subscriptions!");
                    }
                }
                break;
        }
    }
    
    /**
     * Force remote node to subscibe for specified topic.
     * @param nodeIp ip address of the node;
     * @param nodePort port of the node;
     * @param topic topic for subscription;
     */
    public void subscibeRemoteNode(String nodeIp, Integer nodePort, String topic) {
        String nodeKey = nodeIp + ":" + nodePort;
        MessagePeerSender peerSender = this.senderMap.get(nodeKey);
        if (peerSender != null) {
            MessageBus.addSubscription(topic, peerSender);
            peerSender.addSubscription(topic);
        }
    }
    
}
