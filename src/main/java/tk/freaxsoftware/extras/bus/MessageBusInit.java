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
import tk.freaxsoftware.extras.bus.bridge.http.LocalHttpCons;
import tk.freaxsoftware.extras.bus.bridge.http.MessageClientSender;
import tk.freaxsoftware.extras.bus.bridge.http.MessageServer;
import tk.freaxsoftware.extras.bus.bridge.http.RemoteSubscriptionReceiver;
import tk.freaxsoftware.extras.bus.bridge.http.cross.CrossConnectionInit;
import tk.freaxsoftware.extras.bus.bridge.http.cross.CrossConnectionStorage;
import tk.freaxsoftware.extras.bus.bridge.http.cross.CrossNode;
import tk.freaxsoftware.extras.bus.config.MessageBusConfig;
import tk.freaxsoftware.extras.bus.config.PropertyConfigProcessor;
import tk.freaxsoftware.extras.bus.config.pool.PoolType;
import tk.freaxsoftware.extras.bus.config.pool.ThreadPoolConfig;
import tk.freaxsoftware.extras.bus.storage.StorageInterceptor;
import tk.freaxsoftware.extras.bus.storage.StorageInterceptorFactory;

/**
 * Message bus init service. Reads configuration and do all routine.
 * @author Stanislav Nepochatov
 */
public class MessageBusInit {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageBusInit.class);
    
    /**
     * Block executor instance.
     */
    private BlockExecutor executor;
    
    /**
     * Message bus config instance.
     */
    private volatile MessageBusConfig config;
    
    /**
     * Storage interceptor.
     */
    private StorageInterceptor interceptor;
    
    /**
     * Remote subscription receiver (available only on main node).
     */
    private RemoteSubscriptionReceiver remoteSubscriber;
    
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
     * <li>Init storage (if configured);</li>
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
        PropertyConfigProcessor.process(config);
        executor = new BlockExecutor(config.getThreadPoolConfig().buildThreadPool());
        
        if (config.getBridgeServer() != null) {
            MessageServer server = new MessageServer();
            server.init(config.getBridgeServer());
            
            if (config.getBridgeClient() != null) {
                MessageClientSender clientSender = new MessageClientSender(config.getBridgeServer(), config.getBridgeClient());
                MessageBus.addSubscription(GlobalCons.G_SUBSCRIBE_TOPIC, clientSender);
                MessageBus.addSubscription(GlobalCons.G_UNSUBSCRIBE_TOPIC, clientSender);
                MessageBus.addSubscription(LocalHttpCons.L_HTTP_HEARTBEAT_TOPIC, clientSender);
                if (config.getBridgeClient().getAdditionalSubscriptions() != null && config.getBridgeClient().getAdditionalSubscriptions().length > 0) {
                    MessageBus.addSubscriptions(config.getBridgeClient().getAdditionalSubscriptions(), clientSender);
                }
                if (config.getBridgeClient().getCrossConnectionsDemand() != null 
                        && config.getBridgeClient().getCrossConnectionsOffer() != null) {
                    MessageBus.addSubscription(LocalHttpCons.L_HTTP_CROSS_NODE_TOPIC, clientSender);
                    CrossNode node = new CrossNode();
                    node.setNodePort(config.getBridgeServer().isNested() ? spark.Spark.port() : config.getBridgeServer().getHttpPort());
                    node.setOfferTopics(config.getBridgeClient().getCrossConnectionsOffer());
                    node.setDemandTopics(config.getBridgeClient().getCrossConnectionsDemand());
                    
                    MessageBus.addSubscription(LocalHttpCons.L_HTTP_CROSS_NODE_UP_TOPIC, new CrossConnectionInit(config.getBridgeClient().getCrossConnectionsDemand()));
                    
                    MessageBus.fire(LocalHttpCons.L_HTTP_CROSS_NODE_TOPIC, node, 
                            MessageOptions.Builder.newInstance().async().broadcast().build());
                }
            } else {
                remoteSubscriber = config.getBridgeServer().getHeartbeatRate() != null ? new RemoteSubscriptionReceiver(config.getBridgeServer().getHeartbeatRate()) : new RemoteSubscriptionReceiver();
                MessageBus.addSubscription(LocalHttpCons.L_HTTP_SUBSCRIBE_TOPIC, remoteSubscriber);
                MessageBus.addSubscription(LocalHttpCons.L_HTTP_UNSUBSCRIBE_TOPIC, remoteSubscriber);
                MessageBus.addSubscription(LocalHttpCons.L_HTTP_HEARTBEAT_TOPIC, remoteSubscriber);
                if (config.getBridgeServer().getCrossConnections()) {
                    MessageBus.addSubscription(LocalHttpCons.L_HTTP_CROSS_NODE_TOPIC, new CrossConnectionStorage());
                }
            }
        }
        
        interceptor = StorageInterceptorFactory.interceptor(config.getStorage());
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

    public StorageInterceptor getInterceptor() {
        return interceptor;
    }

    public RemoteSubscriptionReceiver getRemoteSubscriber() {
        return remoteSubscriber;
    }
}
