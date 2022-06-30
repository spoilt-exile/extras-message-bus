/*
 * This file is part of MessageBus library.
 * 
 * Copyright (C) 2022 Freax Software
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
package tk.freaxsoftware.extras.bus.bridge.http;

import com.google.gson.reflect.TypeToken;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Type resover holds user defined types of data and token to help in it's deserialization.
 * @author Stanislav Nepochatov
 */
public class TypeResolver {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(TypeResolver.class);
    
    private final static Map<String, TypeToken> registry = new ConcurrentHashMap();
    
    public static void register(String type, TypeToken token) {
        LOGGER.info("Register type {} with token {}", type, token.toString());
        registry.put(type, token);
    }
    
    public static TypeToken resolveType(String type) {
        return registry.get(type);
    }
    
    public static Boolean isTypeRegistered(String type) {
        return registry.containsKey(type);
    }
    
}
