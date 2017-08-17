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

import java.util.Map;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.freaxsoftware.extras.bus.GlobalIds;
import tk.freaxsoftware.extras.bus.MessageBus;
import tk.freaxsoftware.extras.bus.ArgBuilder;
import tk.freaxsoftware.extras.bus.MessageOptions;
import tk.freaxsoftware.extras.bus.exceptions.ReceiverRegistrationException;

/**
 * Generic test of message bus.
 * @author Stanislav Nepochatov
 */
public class MessageBusTest {
    
    private final Logger logger = LoggerFactory.getLogger(MessageBusTest.class);
    
    private static final String INCORRECT_MESSAGE = "MessageTest.INCORRECT_MESSAGE";
    private static final String EMPTY_MESSAGE = "MessageTest.EMPTY_MESSAGE";
    private static final String MULTIPLIE_MESSAGE = "MessageTest.MULTIPLIE_MESSAGE";
    
    private static final String ARG_MULTIPLIE_DIGIT1 = "MessageTest.Arg.MULTIPLIE_DIGIT1";
    private static final String ARG_MULTIPLIE_DIGIT2 = "MessageTest.Arg.MULTIPLIE_DIGIT2";
    
    private static final String RES_MULTIPLIE = "MessageTest.Arg.RES";
    
    private static final String EXCEPTION_MESSAGE = "Test exception";
    
    private static final String ROUND_ROBIN_MESSAGE = "Round.Robin.Test";
    
    public MessageBusTest() {
    }
    
    @Before
    public void setUp() throws ReceiverRegistrationException {
        MessageBus.addSubscription(EMPTY_MESSAGE, (String messageId, Map<String, Object> arguments, Map<String, Object> result) -> {
            logger.debug("empty message received, throwing exception");
            throw new Exception(EXCEPTION_MESSAGE);
        });
        MessageBus.addSubscription(EMPTY_MESSAGE, (String messageId, Map<String, Object> arguments, Map<String, Object> result) -> {
            assertEquals(messageId, EMPTY_MESSAGE);
            logger.warn(messageId + " received!");
        });
        MessageBus.addSubscription(MULTIPLIE_MESSAGE, (String messageId, Map<String, Object> arguments, Map<String, Object> result) -> {
            assertEquals(messageId, MULTIPLIE_MESSAGE);
            logger.warn(messageId + " received!");
            Integer digit1 = (Integer) arguments.get(ARG_MULTIPLIE_DIGIT1);
            Integer digit2 = (Integer) arguments.get(ARG_MULTIPLIE_DIGIT2);
            Integer multiplied = digit1 * digit2;
            result.put(RES_MULTIPLIE, multiplied);
        });
        MessageBus.addSubscription(ROUND_ROBIN_MESSAGE, new RoundRobinReceiver(1));
        MessageBus.addSubscription(ROUND_ROBIN_MESSAGE, new RoundRobinReceiver(2));
        MessageBus.addSubscription(ROUND_ROBIN_MESSAGE, new RoundRobinReceiver(3));
        MessageBus.addSubscription(ROUND_ROBIN_MESSAGE, new RoundRobinReceiver(4));
        MessageBus.addSubscription(ROUND_ROBIN_MESSAGE, new RoundRobinReceiver(5));
    }
    
    @Test
    public void emptyMessage() {
        MessageBus.fire(EMPTY_MESSAGE, null);
    }
    
    @Test
    public void emptyMessageException() {
        MessageBus.fire(EMPTY_MESSAGE, null, MessageOptions.Builder.newInstance().callback((result) -> {
            assertTrue(result.containsKey(GlobalIds.GLOBAL_EXCEPTION));
            Exception last = (Exception) result.get(GlobalIds.GLOBAL_EXCEPTION);
            assertEquals(last.getMessage(), EXCEPTION_MESSAGE);
        }).build());
    }
    
    @Test
    public void multiplieMessage() {
        MessageBus.fire(MULTIPLIE_MESSAGE, ArgBuilder.newInstance().putArg(ARG_MULTIPLIE_DIGIT1, 2).putArg(ARG_MULTIPLIE_DIGIT2, 2).build(), MessageOptions.Builder.newInstance().callback((result) -> {
            logger.warn("result of multiplication; " + result.get(RES_MULTIPLIE));
            assertTrue(result.containsKey(RES_MULTIPLIE));
            Integer resultInt = (Integer) result.get(RES_MULTIPLIE);
            assertEquals(resultInt, new Integer(4));
        }).build());
    }
    
    @Test
    public void testRoundRobin() {
        for (int outer = 0; outer < 3; outer++) {
            for (int inner = 1; inner < 6; inner++) {
                MessageBus.fire(ROUND_ROBIN_MESSAGE, ArgBuilder.newInstance().putArg(RoundRobinReceiver.ROUND_ROBIN_KEY, inner).build());
            }
        }
    }
    
    @After
    public void tearDown() {
        MessageBus.clearBus();
    }
    
}
