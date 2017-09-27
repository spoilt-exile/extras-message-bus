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

import java.util.HashMap;
import java.util.Map;

/**
 * Message header helper;
 * @author Stanislav Nepochatov
 */
public class HeaderBuilder {
    
    private final Map<String, String> args = new HashMap<>();
    
    /**
     * Add pair of key and value to header builder.
     * @param argKey key;
     * @param argValue value;
     * @return builder instance;
     */
    public HeaderBuilder putArg(String argKey, String argValue) {
        this.args.put(argKey, argValue);
        return this;
    }
    
    /**
     * Get headers from builder.
     * @return map of headers;
     */
    public Map<String, String> build() {
        return this.args;
    }
    
    /**
     * Get new builder.
     * @return builder;
     */
    public static final HeaderBuilder newInstance() {
        return new HeaderBuilder();
    }
    
}
