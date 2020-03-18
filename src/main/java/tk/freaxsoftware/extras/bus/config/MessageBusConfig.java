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

package tk.freaxsoftware.extras.bus.config;

import tk.freaxsoftware.extras.bus.config.http.ClientConfig;
import tk.freaxsoftware.extras.bus.config.http.ServerConfig;
import tk.freaxsoftware.extras.bus.config.pool.ThreadPoolConfig;
import tk.freaxsoftware.extras.bus.storage.StorageConfig;

/**
 * Message bus main config class.
 * @author Stanislav Nepochatov
 */
public class MessageBusConfig {
    
    private ThreadPoolConfig threadPoolConfig;
    
    private ServerConfig bridgeServer;
    
    private ClientConfig bridgeClient;
    
    private StorageConfig storage;

    public ThreadPoolConfig getThreadPoolConfig() {
        return threadPoolConfig;
    }

    public void setThreadPoolConfig(ThreadPoolConfig threadPoolConfig) {
        this.threadPoolConfig = threadPoolConfig;
    }

    public ServerConfig getBridgeServer() {
        return bridgeServer;
    }

    public void setBridgeServer(ServerConfig bridgeServer) {
        this.bridgeServer = bridgeServer;
    }

    public ClientConfig getBridgeClient() {
        return bridgeClient;
    }

    public void setBridgeClient(ClientConfig bridgeClient) {
        this.bridgeClient = bridgeClient;
    }

    public StorageConfig getStorage() {
        return storage;
    }

    public void setStorage(StorageConfig storage) {
        this.storage = storage;
    }
}
