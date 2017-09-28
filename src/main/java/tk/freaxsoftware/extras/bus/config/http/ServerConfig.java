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
 * Http bridge server config.
 * @author Stanislav Nepochatov
 */
public class ServerConfig {
    
    private Boolean nested;
    
    private Integer heartbeatRate;
    
    private Integer httpPort;
    
    private Integer sparkThreadPoolMaxSize;

    public Boolean isNested() {
        return nested;
    }

    public void setNested(Boolean nested) {
        this.nested = nested;
    }

    public Integer getHeartbeatRate() {
        return heartbeatRate;
    }

    public void setHeartbeatRate(Integer heartbeatRate) {
        this.heartbeatRate = heartbeatRate;
    }

    public Integer getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(Integer httpPort) {
        this.httpPort = httpPort;
    }

    public Integer getSparkThreadPoolMaxSize() {
        return sparkThreadPoolMaxSize;
    }

    public void setSparkThreadPoolMaxSize(Integer sparkThreadPoolMaxSize) {
        this.sparkThreadPoolMaxSize = sparkThreadPoolMaxSize;
    }
}
