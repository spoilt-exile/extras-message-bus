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
import tk.freaxsoftware.extras.bus.annotation.AnnotationUtil;
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
     * Instance of message server. Can be null on local bus;
     */
    private MessageServer server;
    
    /**
     * Remote subscription receiver (for central node only). Can be null on local bus or client node.
     */
    private RemoteSubscriptionReceiver remoteSubscriber;
    
    /**
     * Message client sender. Available only on client node.
     */
    private MessageClientSender clientSender;
    
    /**
     * Storage for cross connections. Available only on central node.
     */
    private CrossConnectionStorage crossConnectionStorage;
    
    /**
     * Inits message bus config and additional components. 
     * Trying to read default config {@code bus_default.json} in main resources folder at first. 
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
     * @param configFileName filename of the config to load;
     */
    protected void ensureInit(String configFileName) {
        if (config != null) {
            return;
        }
        LOGGER.info("Init message bus");
        MessageBusConfig defaultConfig = readDefault();
        MessageBusConfig standardConfig = readStandard(configFileName);
        
        config = standardConfig == null ? defaultConfig : standardConfig;
        PropertyConfigProcessor.process(config);
        executor = new BlockExecutor(config.getThreadPoolConfig().buildThreadPool());
        
        interceptor = StorageInterceptorFactory.interceptor(config.getStorage());
        
        if (config.getBridgeServer() != null) {
            server = new MessageServer();
            server.init(config.getBridgeServer(), interceptor);
            
            if (config.getBridgeClient() != null) {
                clientSender = new MessageClientSender(config.getBridgeServer(), config.getBridgeClient());
                MessageBus.addSubscription(GlobalCons.G_SUBSCRIBE_TOPIC, clientSender);
                MessageBus.addSubscription(GlobalCons.G_UNSUBSCRIBE_TOPIC, clientSender);
                MessageBus.addSubscription(LocalHttpCons.L_HTTP_HEARTBEAT_TOPIC, clientSender);
                if (config.getBridgeClient().getAdditionalSubscriptions() != null && config.getBridgeClient().getAdditionalSubscriptions().length > 0) {
                    MessageBus.addSubscriptions(config.getBridgeClient().getAdditionalSubscriptions(), clientSender);
                }
                if (config.getBridgeClient().getCrossConnectionsSends() != null 
                        && config.getBridgeClient().getCrossConnectionsReceives() != null) {
                    MessageBus.addSubscription(LocalHttpCons.L_HTTP_CROSS_NODE_TOPIC, clientSender);
                    CrossNode node = new CrossNode();
                    node.setNodePort(config.getBridgeServer().getHttpPort());
                    node.setReceiveTopics(config.getBridgeClient().getCrossConnectionsReceives());
                    node.setSendTopics(config.getBridgeClient().getCrossConnectionsSends());
                    node.setTag(config.getBridgeClient().getTag());
                    
                    AnnotationUtil.subscribeReceiverInstance(new CrossConnectionInit(config.getBridgeClient().getCrossConnectionsSends()));
                    
                    MessageBus.fire(LocalHttpCons.L_HTTP_CROSS_NODE_TOPIC, node, 
                            MessageOptions.Builder.newInstance().async().broadcast().build());
                }
            } else {
                remoteSubscriber = config.getBridgeServer().getHeartbeatRate() != null 
                        ? new RemoteSubscriptionReceiver(config.getBridgeServer().getCrossConnections(), config.getBridgeServer().getHeartbeatRate()) 
                        : new RemoteSubscriptionReceiver(config.getBridgeServer().getCrossConnections());
                MessageBus.addSubscription(LocalHttpCons.L_HTTP_SUBSCRIBE_TOPIC, remoteSubscriber);
                MessageBus.addSubscription(LocalHttpCons.L_HTTP_UNSUBSCRIBE_TOPIC, remoteSubscriber);
                MessageBus.addSubscription(LocalHttpCons.L_HTTP_HEARTBEAT_TOPIC, remoteSubscriber);
                if (config.getBridgeServer().getCrossConnections()) {
                    crossConnectionStorage = new CrossConnectionStorage();
                    MessageBus.addSubscription(LocalHttpCons.L_HTTP_CROSS_NODE_TOPIC, crossConnectionStorage);
                }
            }
        }
    }
    
    private MessageBusConfig readDefault() {
        Reader jsonReader = new InputStreamReader(getClass().getClassLoader().getResourceAsStream("bus_default.json"));
        return readConfig(jsonReader);
    }
    
    private MessageBusConfig readStandard(String configFileName) {
        try {
            Reader jsonReader = new InputStreamReader(getClass().getClassLoader().getResourceAsStream(configFileName));
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

    public MessageServer getServer() {
        return server;
    }

    public RemoteSubscriptionReceiver getRemoteSubscriber() {
        return remoteSubscriber;
    }

    public MessageClientSender getClientSender() {
        return clientSender;
    }

    public CrossConnectionStorage getCrossConnectionStorage() {
        return crossConnectionStorage;
    }
}
