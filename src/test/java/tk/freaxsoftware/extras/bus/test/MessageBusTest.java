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

import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.freaxsoftware.extras.bus.Callback;
import tk.freaxsoftware.extras.bus.MessageBus;
import tk.freaxsoftware.extras.bus.Receiver;

/**
 * Generic test of message bus.
 * @author Stanislav Nepochatov
 */
public class MessageBusTest {
    
    private final Logger logger = LoggerFactory.getLogger(MessageBusTest.class);
    
    private static final String EMPTY_MESSAGE = "MessageTest.EMPTY_MESSAGE";
    private static final String MULTIPLIE_MESSAGE = "MessageTest.MULTIPLIE_MESSAGE";
    
    private static final String ARG_MULTIPLIE_DIGIT1 = "MessageTest.Arg.MULTIPLIE_DIGIT1";
    private static final String ARG_MULTIPLIE_DIGIT2 = "MessageTest.Arg.MULTIPLIE_DIGIT2";
    
    private static final String RES_MULTIPLIE = "MessageTest.Arg.RES";
    
    public MessageBusTest() {
    }
    
    @Before
    public void setUp() {
        MessageBus.addSubscription(EMPTY_MESSAGE, new Receiver() {

            @Override
            public void receive(String messageId, Map<String, Object> arguments, Map<String, Object> result) {
                assertNotNull(messageId);
                logger.warn(messageId + " received!");
            }
        });
        MessageBus.addSubscription(MULTIPLIE_MESSAGE, new Receiver() {

            @Override
            public void receive(String messageId, Map<String, Object> arguments, Map<String, Object> result) {
                logger.warn(messageId + " received!");
                Integer digit1 = (Integer) arguments.get(ARG_MULTIPLIE_DIGIT1);
                Integer digit2 = (Integer) arguments.get(ARG_MULTIPLIE_DIGIT2);
                Integer multiplied = digit1 * digit2;
                result.put(RES_MULTIPLIE, multiplied);
            }
        });
    }
    
    @Test
    public void emptyMessage() {
        MessageBus.fireMessageSync(EMPTY_MESSAGE, null, null);
    }
    
    @Test
    public void multiplieMessage() {
        Map<String, Object> args = new HashMap<>();
        args.put(ARG_MULTIPLIE_DIGIT1, 2);
        args.put(ARG_MULTIPLIE_DIGIT2, 2);
        MessageBus.fireMessageSync(MULTIPLIE_MESSAGE, args, new Callback() {

            @Override
            public void callback(Map<String, Object> result) {
                logger.warn("result of multiplication; " + result.get(RES_MULTIPLIE));
            }
        });
    }
    
    @After
    public void tearDown() {
    }
    
}
