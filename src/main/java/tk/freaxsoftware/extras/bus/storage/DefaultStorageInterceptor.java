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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import tk.freaxsoftware.extras.bus.MessageBus;
import tk.freaxsoftware.extras.bus.MessageHolder;
import tk.freaxsoftware.extras.bus.MessageOptions;

/**
 * Default storage interceptor.
 * @author Stanislav Nepochatov
 * @since 5.0
 */
public class DefaultStorageInterceptor implements StorageInterceptor {
    
    private final StorageConfig config;
    
    private final MessageStorage storage;
    
    private final ExecutorService threadService = Executors.newSingleThreadExecutor();

    public DefaultStorageInterceptor(StorageConfig config) throws StorageInitException {
        this.config = config;
        this.storage = initStorage();
        this.threadService.submit(new RedeliveryJob(config.getRedeliveryPeriod(), storage));
    }
    
    private MessageStorage initStorage() throws StorageInitException {
        try {
            Class storageClass = Class.forName(config.getStorageClass());
            if (config.getStorageClassArgs() != null && !config.getStorageClassArgs().isEmpty()) {
                Constructor<MessageStorage> paramConstructor = storageClass.getConstructor(Map.class);
                return paramConstructor.newInstance(config.getStorageClassArgs());
            } else {
                Constructor<MessageStorage> defaultConstructor = storageClass.getConstructor();
                return defaultConstructor.newInstance();
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

    @Override
    public void storeMessage(MessageHolder holder) {
        if (holder.getTopic().matches(config.getTopicPattern())) {
            if (holder.getOptions().getDeliveryPolicy() != MessageOptions.DeliveryPolicy.VOID
                    && (holder.getOptions().getDeliveryPolicy() == MessageOptions.DeliveryPolicy.CALL && config.getStoreCalls())) {
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
            Set<MessageHolder> holders = storage.getUnprocessedMessages();
            for (MessageHolder holder: holders) {
                MessageBus.internalFire(holder);
            }
            try {
                Thread.sleep(redeliveryPeriod * 1000);
            } catch (InterruptedException ex) {
                //Nothing.
            }
        }
        
    }
}
