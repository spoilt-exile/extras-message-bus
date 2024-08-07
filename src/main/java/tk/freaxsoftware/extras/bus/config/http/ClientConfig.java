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
package tk.freaxsoftware.extras.bus.config.http;

/**
 * HTTP cleint config.
 * @author Stanislav Nepochatov
 */
public class ClientConfig {
    
    private String tag;
    
    private String address;
    
    private Integer port;
    
    private Integer heartbeatRate;
    
    private String[] additionalSubscriptions;
    
    private String[] crossConnectionsReceives;
    
    private String[] crossConnectionsSends;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getHeartbeatRate() {
        return heartbeatRate;
    }

    public void setHeartbeatRate(Integer heartbeatRate) {
        this.heartbeatRate = heartbeatRate;
    }

    public String[] getAdditionalSubscriptions() {
        return additionalSubscriptions;
    }

    public void setAdditionalSubscriptions(String[] additionalSubscriptions) {
        this.additionalSubscriptions = additionalSubscriptions;
    }

    public String[] getCrossConnectionsReceives() {
        return crossConnectionsReceives;
    }

    public void setCrossConnectionsReceives(String[] crossConnectionsReceives) {
        this.crossConnectionsReceives = crossConnectionsReceives;
    }

    public String[] getCrossConnectionsSends() {
        return crossConnectionsSends;
    }

    public void setCrossConnectionsSends(String[] crossConnectionsSends) {
        this.crossConnectionsSends = crossConnectionsSends;
    }
}
