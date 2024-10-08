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

import java.util.concurrent.ExecutorService;
import tk.freaxsoftware.extras.bus.executor.MessageExecutor;

/**
 * Block executor allows to execute block of codes in sync and async manner.
 * @author Stanislav Nepochatov
 */
public class BlockExecutor {
    
    /**
     * Thread pool executor.
     */
    private final ExecutorService threadService;

    public BlockExecutor(ExecutorService threadService) {
        this.threadService = threadService;
    }
    
    /**
     * Execute block of code in manner specified by argument.
     * @param block block of code;
     * @param async async flag;
     */
    public void execute(MessageExecutor block, Boolean async) {
        if (async) {
            executeAsync(block);
        } else {
            executeSync(block);
        }
    } 
    
    /**
     * Execute in sync mode.
     * @param block code block;
     */
    public void executeSync(MessageExecutor block) {
        block.exec();
    }
    
    /**
     * Execute in async mode.
     * @param block code block;
     */
    public void executeAsync(MessageExecutor block) {
        threadService.submit(() -> block.exec());
    }
    
    public void executeAsync(Runnable runnable) {
        threadService.submit(runnable);
    }
    
}
