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

package tk.freaxsoftware.extras.bus.config.pool;

/**
 * Type of the thread pool.
 * @author Stanislav Nepochatov
 */
public enum PoolType {
    FIXED_POOL(FixedThreadPoolConfig.class),
    FORK_JOIN_POOL(ForkJoinThreadPoolConfig.class),
    SINLGE_POOL(SingleThreadPoolConfig.class),
    CACHED_POOL(CachedThreadPoolConfig.class);
    
    private Class poolClass;
    
    private PoolType(Class poolClass) {
        this.poolClass = poolClass;
    }

    public Class getPoolClass() {
        return poolClass;
    }
}
