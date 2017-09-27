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

package tk.freaxsoftware.extras.bus;

import com.google.gson.JsonElement;
import io.gsonfire.GsonFireBuilder;
import java.io.InputStreamReader;
import java.io.Reader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.freaxsoftware.extras.bus.bridge.http.LocalHttpIds;
import tk.freaxsoftware.extras.bus.bridge.http.MessageClientSender;
import tk.freaxsoftware.extras.bus.bridge.http.MessageServer;
import tk.freaxsoftware.extras.bus.bridge.http.RemoteSubscriptionReceiver;
import tk.freaxsoftware.extras.bus.config.MessageBusConfig;
import tk.freaxsoftware.extras.bus.config.pool.PoolType;
import tk.freaxsoftware.extras.bus.config.pool.ThreadPoolConfig;

/**
 * Message bus init service. Reads configuration and do all routine.
 * @author Stanislav Nepochatov
 */
public class MessageBusInit {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageBus.class);
    
    /**
     * Block executor instance.
     */
    private BlockExecutor executor;
    
    /**
     * Message bus config instance.
     */
    private volatile MessageBusConfig config;
    
    /**
     * Inits message bus config and additional components. Trying to read 
     * default config {@code bus_default.json} in main resources folder at first. 
     * Also it reads {@code bus.json} standard configuration file in resources. 
     * Standard config has priority over defaults.<br/>
     * <br/>
     * List of routines:
     * <ol>
     * <li>Read default config;</li>
     * <li>Read standard config;</li>
     * <li>Choose config file;</li>
     * <li>Creating block executor instance;</li>
     * <li>Establish HTTP server node (if configured);</li>
     * <li>Establish HTTP client sender (if server and client both configured) or creating instance of {@code RemoteSubscriptionReceiver};</li>
     * </ol>
     */
    protected void ensureInit() {
        if (config != null) {
            return;
        }
        LOGGER.info("Init message bus");
        MessageBusConfig defaultConfig = readDefault();
        MessageBusConfig standardConfig = readStandard();
        
        config = standardConfig == null ? defaultConfig : standardConfig;
        executor = new BlockExecutor(config.getThreadPoolConfig().buildThreadPool());
        
        if (config.getBridgeServer() != null) {
            MessageServer server = new MessageServer();
            server.init(config.getBridgeServer());
            
            if (config.getBridgeClient() != null) {
                MessageClientSender clientSender = new MessageClientSender(config.getBridgeServer(), config.getBridgeClient());
                MessageBus.addSubscription(GlobalIds.GLOBAL_SUBSCRIBE, clientSender);
                MessageBus.addSubscription(GlobalIds.GLOBAL_UNSUBSCRIBE, clientSender);
            } else {
                RemoteSubscriptionReceiver remoteSubscriber = new RemoteSubscriptionReceiver();
                MessageBus.addSubscription(LocalHttpIds.LOCAL_HTTP_MESSAGE_SUBSCRIBE, remoteSubscriber);
                MessageBus.addSubscription(LocalHttpIds.LOCAL_HTTP_MESSAGE_UNSUBSCRIBE, remoteSubscriber);
            }
        }
        
    }
    
    private MessageBusConfig readDefault() {
        Reader jsonReader = new InputStreamReader(getClass().getClassLoader().getResourceAsStream("bus_default.json"));
        return readConfig(jsonReader);
    }
    
    private MessageBusConfig readStandard() {
        try {
            Reader jsonReader = new InputStreamReader(getClass().getClassLoader().getResourceAsStream("bus.json"));
            return readConfig(jsonReader);
        } catch (Exception ex) {
            LOGGER.error("Unable to read standard config!", ex);
            return null;
        }
    }
    
    private MessageBusConfig readConfig(Reader configReader) {
        GsonFireBuilder builder = new GsonFireBuilder().registerTypeSelector(ThreadPoolConfig.class, (JsonElement je) -> {
            String type = je.getAsJsonObject().get("type").getAsString();
            PoolType enumType = PoolType.valueOf(type);
            return enumType.getPoolClass();
        });
        return builder.createGson().fromJson(configReader, MessageBusConfig.class);
    }

    public BlockExecutor getExecutor() {
        return executor;
    }
}
