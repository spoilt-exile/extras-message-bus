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

package tk.freaxsoftware.extras.bus.test;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.freaxsoftware.extras.bus.MessageHolder;
import tk.freaxsoftware.extras.bus.Receiver;

/**
 * Test receiver for round-robin-testing.
 * @author Stanislav Nepochatov
 */
public class RoundRobinReceiver implements Receiver {
    
    private final Logger logger = LoggerFactory.getLogger(RoundRobinReceiver.class);
    
    public static final String ROUND_ROBIN_KEY = "index";
    
    private final int index;

    public RoundRobinReceiver(int index) {
        this.index = index;
    }

    @Override
    public void receive(MessageHolder holder) throws Exception {
        logger.info(String.format("Receiver #%d gets message with index %d", index, holder.getContent()));
        Assert.assertEquals(index, holder.getContent());
    }
    
}
