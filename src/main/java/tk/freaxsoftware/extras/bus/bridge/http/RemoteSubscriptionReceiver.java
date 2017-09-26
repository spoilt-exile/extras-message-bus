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
import java.util.concurrent.ConcurrentHashMap;
import tk.freaxsoftware.extras.bus.GlobalIds;
import tk.freaxsoftware.extras.bus.MessageBus;
import tk.freaxsoftware.extras.bus.MessageHolder;
import tk.freaxsoftware.extras.bus.Receiver;

/**
 * Remote sender 
 * @author Stanislav Nepochatov
 */
public class RemoteSubscriptionReceiver implements Receiver {
    
    private final Map<String, MessagePeerSender> senderMap;

    public RemoteSubscriptionReceiver() {
        senderMap = new ConcurrentHashMap<>();
    }

    @Override
    public void receive(MessageHolder message) throws Exception {
        String subscriptionId = (String) message.getHeaders().get(GlobalIds.GLOBAL_HEADER_SUBSCRIPTION_ID);
        String nodeIp = (String) message.getHeaders().get(LocalHttpIds.LOCAL_HTTP_HEADER_NODE_IP);
        Integer nodePort = Integer.parseInt((String) message.getHeaders().get(LocalHttpIds.LOCAL_HTTP_HEADER_NODE_PORT));
        switch (message.getMessageId()) {
            case LocalHttpIds.LOCAL_HTTP_MESSAGE_SUBSCRIBE:
                MessagePeerSender peerSender;
                if (!senderMap.containsKey(nodeIp)) {
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
                    senderMap.remove(nodeIp);
                    MessageBus.removeSubscription(subscriptionId, peerSender2);
                }
        }
    }
    
}
