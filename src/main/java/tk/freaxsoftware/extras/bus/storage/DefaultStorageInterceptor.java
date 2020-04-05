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
package tk.freaxsoftware.extras.bus.storage;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.freaxsoftware.extras.bus.MessageBus;
import tk.freaxsoftware.extras.bus.MessageHolder;
import tk.freaxsoftware.extras.bus.MessageOptions;
import tk.freaxsoftware.extras.bus.MessageStatus;

/**
 * Default storage interceptor.
 * @author Stanislav Nepochatov
 * @since 5.0
 */
public class DefaultStorageInterceptor implements StorageInterceptor {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultStorageInterceptor.class);
    
    private final StorageConfig config;
    
    private final MessageStorage storage;
    
    private final ExecutorService threadService = Executors.newFixedThreadPool(2);
    
    private List<GroupingReceiver> grouping;

    public DefaultStorageInterceptor(StorageConfig config) throws StorageInitException {
        this.config = config;
        this.storage = initStorage();
        this.initGrouping();
        this.threadService.submit(new RedeliveryJob(config.getRedeliveryPeriod(), storage));
        if (config.getGroupingScanPeriod() != null) {
            this.threadService.submit(new GroupScanJob(config.getGroupingScanPeriod(), grouping));
        }
    }
    
    private MessageStorage initStorage() throws StorageInitException {
        try {
            Class storageClass = Class.forName(config.getStorageClass());
            if (config.getStorageClassArgs() != null && !config.getStorageClassArgs().isEmpty()) {
                Constructor<MessageStorage> paramConstructor = storageClass.getConstructor(Map.class);
                return paramConstructor.newInstance(config.getStorageClassArgs());
            } else {
                Constructor<MessageStorage> defaultConstructor = storageClass.getConstructor();
                MessageStorage instance = defaultConstructor.newInstance();
                LOGGER.info("Storage class {} initiated.", config.getStorageClass());
                return instance;
            }
        } catch (ClassNotFoundException clnfex) {
            throw new StorageInitException(String.format("Storage class %s was not found!", config.getStorageClass()), clnfex);
        } catch (NoSuchMethodException nsmtex) {
            throw new StorageInitException(String.format("Storage class %s doesn't have default contructor or constructor to accept Map<String,String>.", config.getStorageClass()), nsmtex);
        } catch (InstantiationException intex) {
            throw new StorageInitException(String.format("Error during making instance of %s storage class.", config.getStorageClass()), intex);
        } catch (IllegalAccessException | InvocationTargetException ilaex) {
            throw new StorageInitException(String.format("Can't get access to constructor of %s storage class.", config.getStorageClass()), ilaex);
        }
    }
    
    private void initGrouping() {
        if (config.getGrouping() != null && !config.getGrouping().isEmpty()) {
            grouping = config.getGrouping().stream().map(grItem -> new GroupingReceiver(grItem, storage)).collect(Collectors.toList());
            for (GroupingReceiver entry: grouping) {
                MessageBus.addSubscription(entry.getTopic(), entry);
            }
        }
    }

    @Override
    public void storeMessage(MessageHolder holder) {
        if (holder.getTopic().matches(config.getTopicPattern())) {
            if (holder.getOptions().getDeliveryPolicy() != MessageOptions.DeliveryPolicy.VOID
                    || (holder.getOptions().getDeliveryPolicy() == MessageOptions.DeliveryPolicy.CALL && config.getStoreCalls())) {
                storage.saveMessage(holder);
            }
        }
    }

    @Override
    public void storeProcessedMessage(MessageHolder holder) {
        if (config.getRemoveProcessed()) {
            storage.removeMessage(holder.getId());
        } else {
            storeMessage(holder);
        }
    }
    
    private class RedeliveryJob implements Runnable {
        
        private final Integer redeliveryPeriod;
        
        private final MessageStorage storage;

        public RedeliveryJob(Integer redeliveryPeriod, MessageStorage storage) {
            this.redeliveryPeriod = redeliveryPeriod;
            this.storage = storage;
        }

        @Override
        public void run() {
            LOGGER.info("Redelivery job started with period {} seconds", redeliveryPeriod);
            while (true) {
                Set<MessageHolder> holders = storage.getUnprocessedMessages();
                LOGGER.info("Redelivery entries batch size {}", holders.size());
                for (MessageHolder holder: holders) {
                    if ((config.getRedeliveryOnlyIfReceiversExists() && MessageBus.isSubscribed(holder.getTopic())) 
                                || !config.getRedeliveryOnlyIfReceiversExists()) {
                        if (holder.getRedeliveryCounter() == 0) {
                            LOGGER.warn("Message {} on topic {} exhaust redelivery attempts, dropping.", 
                                    holder.getId(), holder.getTopic());
                            holder.setStatus(MessageStatus.EXHAUSTED);
                            storeProcessedMessage(holder);
                            continue;
                        }
                        LOGGER.info("Processing message {} to topic {} attempts left {}", 
                                holder.getId(), holder.getTopic(), holder.getRedeliveryCounter());
                        holder.decreaseRedeliveryCounter();
                        MessageBus.internalFire(holder);
                    }
                }
                try {
                    Thread.sleep(redeliveryPeriod * 1000);
                } catch (InterruptedException ex) {
                    //Nothing.
                }
            }
        }
        
    }
    
    private class GroupScanJob implements Runnable {
        
        private final Integer groupingScanPeriod;
        
        private final List<GroupingReceiver> receivers;

        public GroupScanJob(Integer groupingScanPeriod, List<GroupingReceiver> receivers) {
            this.groupingScanPeriod = groupingScanPeriod;
            this.receivers = receivers;
        }

        @Override
        public void run() {
            LOGGER.info("Grouping scan job started with period {} seconds", groupingScanPeriod);
            while (true) {
                receivers.forEach(rc -> rc.sendMessagesByTimeout());
                try {
                    Thread.sleep(groupingScanPeriod * 1000);
                } catch (InterruptedException ex) {
                    //Nothing.
                }
            }
        }
        
    }
}
