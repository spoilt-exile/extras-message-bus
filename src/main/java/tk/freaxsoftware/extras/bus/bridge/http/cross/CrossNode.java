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
import java.util.Objects;

/**
 * Holder for cross node message.
 * @author Stanislav Nepochatov
 * @see 5.0
 */
public class CrossNode {
    
    private String tag;
    
    private String nodeIp;
    
    private Integer nodePort;
    
    private String[] receiveTopics;
    
    private String[] sendTopics;

    public CrossNode() {
    }

    public CrossNode(String nodeIp, Integer nodePort) {
        this.nodeIp = nodeIp;
        this.nodePort = nodePort;
    }

    public CrossNode(String nodeIp, Integer nodePort, String[] receiveTopics, String[] sendTopics) {
        this.nodeIp = nodeIp;
        this.nodePort = nodePort;
        this.receiveTopics = receiveTopics;
        this.sendTopics = sendTopics;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getNodeIp() {
        return nodeIp;
    }

    public void setNodeIp(String nodeIp) {
        this.nodeIp = nodeIp;
    }

    public Integer getNodePort() {
        return nodePort;
    }

    public void setNodePort(Integer nodePort) {
        this.nodePort = nodePort;
    }

    public String[] getReceiveTopics() {
        return receiveTopics;
    }

    public void setReceiveTopics(String[] receiveTopics) {
        this.receiveTopics = receiveTopics;
    }

    public String[] getSendTopics() {
        return sendTopics;
    }

    public void setSendTopics(String[] sendTopics) {
        this.sendTopics = sendTopics;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.nodeIp);
        hash = 29 * hash + Objects.hashCode(this.nodePort);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CrossNode other = (CrossNode) obj;
        if (!Objects.equals(this.tag, other.tag)) {
            return false;
        }
        if (!Objects.equals(this.nodeIp, other.nodeIp)) {
            return false;
        }
        if (!Objects.equals(this.nodePort, other.nodePort)) {
            return false;
        }
        if (!Arrays.deepEquals(this.receiveTopics, other.receiveTopics)) {
            return false;
        }
        if (!Arrays.deepEquals(this.sendTopics, other.sendTopics)) {
            return false;
        }
        return true;
    }
}
