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

package tk.freaxsoftware.extras.bus.annotation;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.freaxsoftware.extras.bus.MessageBus;
import tk.freaxsoftware.extras.bus.MessageHolder;

/**
 * Utility class to process annotation driven receivers.
 * @author Stanislav Nepochatov
 */
public class AnnotationUtil {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationUtil.class);
    
    private static final Map<Class, List<ReflectReceiver>> reflectionMap = new ConcurrentHashMap<>();
    
    /**
     * Subscribes all annotated method of the specified class.
     * @param receiverClass class to subscribe;
     * @throws java.lang.InstantiationException
     * @throws java.lang.IllegalAccessException
     */
    public static void subscribeReceiverClass(Class receiverClass) throws InstantiationException, IllegalAccessException {
        Object instance = receiverClass.newInstance();
        subscribeReceiverInstance(instance);
    }
    
    /**
     * Subscribe all annotated methods of the specified instance.
     * @param instance some class intance to subscribe;
     */
    public static void subscribeReceiverInstance(Object instance) {
        List<ReflectReceiver> receivers = getReflectReceivers(instance);
        if (!receivers.isEmpty()) {
            for (ReflectReceiver receiver: receivers) {
                MessageBus.addSubscriptions(receiver.getSubscriptions(), receiver);
            }
            reflectionMap.put(instance.getClass(), receivers);
        } else {
            LOGGER.info(String.format("No subscriptions for class %s", instance.getClass().getCanonicalName()));
        }
    }
    
    /**
     * Unsubscribes specified class.
     * @param receiverClass class to unsubscribe;
     */
    public static void unsubscribeReceiverClass(Class receiverClass) {
        List<ReflectReceiver> receivers = reflectionMap.get(receiverClass);
        if (receivers != null) {
            for (ReflectReceiver receiver: receivers) {
                MessageBus.removeSubscriptions(receiver.getSubscriptions(), receiver);
            }
            reflectionMap.remove(receiverClass);
        }
    }
    
    /**
     * Unsubscribe specified instance.
     * @param instance some class instance to subscribe;
     */
    public static void unsubscribeReceiverInstance(Object instance) {
        unsubscribeReceiverClass(instance.getClass());
    }
    
    /**
     * Get list of the reflect method receivers from instance.
     * @param instance 
     * @return list of the ready-to-use receivers;
     */
    private static List<ReflectReceiver> getReflectReceivers(Object instance) {
        List<ReflectReceiver> receivers = new ArrayList<>();
        Method[] methods = instance.getClass().getMethods();
        for (Method method: methods) {
            Receive receiveAnnotation = method.getAnnotation(Receive.class);
            if (receiveAnnotation == null) {
                continue;
            }
            if (!Modifier.isPublic(method.getModifiers())) {
                LOGGER.info(String.format("Skipping method %s cause it's not public.", method.getName()));
                continue;
            }
            if (!(method.getAnnotatedParameterTypes().length == 1 && Objects.equals(method.getParameterTypes()[0].getTypeName(), MessageHolder.class.getTypeName()))) {
                LOGGER.info(String.format("Skipping method %s cause incompatibility of the argument signature.", method.getName()));
                continue;
            }
            receivers.add(new ReflectReceiver(receiveAnnotation.value(), method, instance));
        }
        return receivers;
    }
    
}
