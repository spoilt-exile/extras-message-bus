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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.freaxsoftware.extras.bus.GlobalIds;
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
        String subscriptionId = (String) message.getHeaders().get(GlobalIds.GLOBAL_HEADER_SUBSCRIPTION_ID);
        String nodeIp = (String) message.getHeaders().get(LocalHttpIds.LOCAL_HTTP_HEADER_NODE_IP);
        Integer nodePort = Integer.parseInt((String) message.getHeaders().get(LocalHttpIds.LOCAL_HTTP_HEADER_NODE_PORT));
        LOGGER.info(String.format("Getting message %s from node %s on port %d", message.getMessageId(), nodeIp, nodePort));
        switch (message.getMessageId()) {
            case LocalHttpIds.LOCAL_HTTP_MESSAGE_SUBSCRIBE:
                MessagePeerSender peerSender;
                if (!senderMap.containsKey(nodeIp)) {
                    LOGGER.info("Creating new subscriber for node.");
                    peerSender = new MessagePeerSender(nodeIp, nodePort);
                    senderMap.put(nodeIp, peerSender);
                } else {
                    peerSender = senderMap.get(nodeIp);
                }
                peerSender.addSubscription(subscriptionId);
                MessageBus.addSubscription(subscriptionId, peerSender);
                break;
            case LocalHttpIds.LOCAL_HTTP_MESSAGE_UNSUBSCRIBE:
                if (senderMap.containsKey(nodeIp)) {
                    MessagePeerSender peerSender2 = senderMap.get(nodeIp);
                    peerSender2.removeSubscription(subscriptionId);
                    MessageBus.removeSubscription(subscriptionId, peerSender2);
                    if (peerSender2.isEmpty()) {
                        LOGGER.info("Removing subscriber for node.");
                        senderMap.remove(nodeIp);
                    }
                }
                break;
            case LocalHttpIds.LOCAL_HTTP_MESSAGE_HEARTBEAT:
                if (senderMap.containsKey(nodeIp)) {
                    MessagePeerSender peerSender3 = senderMap.get(nodeIp);
                    peerSender3.beat();
                } else {
                    LOGGER.info("Reinit connection for " + nodeIp + " on port " + nodePort);
                    MessagePeerSender peerSender4 = new MessagePeerSender(nodeIp, nodePort);
                    senderMap.put(nodeIp, peerSender4);
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
    
}
