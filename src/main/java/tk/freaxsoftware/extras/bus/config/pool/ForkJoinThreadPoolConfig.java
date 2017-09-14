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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Fork join thread pool config class.
 * @author Stanislav Nepochatov
 */
public class ForkJoinThreadPoolConfig extends ThreadPoolConfig {
    
    private Integer cpuThreadCount;

    public Integer getCpuThreadCount() {
        return cpuThreadCount;
    }

    public void setCpuThreadCount(Integer cpuThreadCount) {
        this.cpuThreadCount = cpuThreadCount;
    }
    
    @Override
    public ExecutorService buildThreadPool() {
        return Executors.newFixedThreadPool(cpuThreadCount);
    }
    
}
