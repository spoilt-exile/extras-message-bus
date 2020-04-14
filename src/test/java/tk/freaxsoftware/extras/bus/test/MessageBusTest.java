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

import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.freaxsoftware.extras.bus.Callback;
import tk.freaxsoftware.extras.bus.GlobalCons;
import tk.freaxsoftware.extras.bus.MessageBus;
import tk.freaxsoftware.extras.bus.MessageContext;
import tk.freaxsoftware.extras.bus.MessageContextHolder;
import tk.freaxsoftware.extras.bus.MessageHolder;
import tk.freaxsoftware.extras.bus.MessageOptions;
import tk.freaxsoftware.extras.bus.ResponseHolder;
import tk.freaxsoftware.extras.bus.exceptions.NoSubscriptionMessageException;
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
    
    private static final String EXCEPTION_CLASS = "java.lang.Exception";
    private static final String EXCEPTION_MESSAGE = "Testing message";
    
    private static final String ROUND_ROBIN_MESSAGE = "Round.Robin.Test";
    private static final String ROUND_ROBIN_REDELIVERY = "Round.Robin.Redelivery";
    
    public MessageBusTest() {
    }
    
    @Before
    public void setUp() throws ReceiverRegistrationException {
        MessageBus.addSubscription(EMPTY_MESSAGE, (MessageHolder holder) -> {
            logger.debug("empty message received, throwing exception");
            throw new Exception(EXCEPTION_MESSAGE);
        });
        MessageBus.addSubscription(EMPTY_MESSAGE, (MessageHolder holder) -> {
            assertEquals(holder.getTopic(), EMPTY_MESSAGE);
            logger.warn(holder.getTopic() + " received!");
        });
        MessageBus.addSubscription(MULTIPLIE_MESSAGE, (MessageHolder holder) -> {
            assertEquals(holder.getTopic(), MULTIPLIE_MESSAGE);
            logger.warn(holder.getTopic() + " received!");
            Integer digit1 = Integer.parseInt((String) holder.getHeaders().get(ARG_MULTIPLIE_DIGIT1));
            Integer digit2 = Integer.parseInt((String) holder.getHeaders().get(ARG_MULTIPLIE_DIGIT2));
            Integer multiplied = digit1 * digit2;
            holder.getResponse().setContent(multiplied);
        });
        MessageBus.addSubscription(ROUND_ROBIN_MESSAGE, new RoundRobinReceiver(1));
        MessageBus.addSubscription(ROUND_ROBIN_MESSAGE, new RoundRobinReceiver(2));
        MessageBus.addSubscription(ROUND_ROBIN_MESSAGE, new RoundRobinReceiver(3));
        MessageBus.addSubscription(ROUND_ROBIN_MESSAGE, new RoundRobinReceiver(4));
        MessageBus.addSubscription(ROUND_ROBIN_MESSAGE, new RoundRobinReceiver(5));
    }
    
    @Test
    public void emptyMessage() {
        MessageBus.fire(EMPTY_MESSAGE, MessageOptions.defaultOptions(null));
    }
    
    @Test(expected = NoSubscriptionMessageException.class)
    public void incorrectMessage() {
        MessageBus.fire(INCORRECT_MESSAGE, MessageOptions.Builder.newInstance().deliveryCall().build());
    }
    
    @Test
    public void emptyMessageException() {
        MessageBus.fire(EMPTY_MESSAGE, null, MessageOptions.Builder.newInstance().callback((result) -> {
            assertTrue(result.getHeaders().containsKey(GlobalCons.G_EXCEPTION_HEADER));
            String last = (String) result.getHeaders().get(GlobalCons.G_EXCEPTION_HEADER);
            String lastMessage = (String) result.getHeaders().get(GlobalCons.G_EXCEPTION_MESSAGE_HEADER);
            assertEquals(last, EXCEPTION_CLASS);
            assertEquals(lastMessage, EXCEPTION_MESSAGE);
        }).build());
    }
    
    @Test
    public void multiplieMessage() {
        MessageBus.fire(MULTIPLIE_MESSAGE, MessageOptions.Builder.newInstance().header(ARG_MULTIPLIE_DIGIT1, "2").header(ARG_MULTIPLIE_DIGIT2, "2").callback((result) -> {
            assertNotNull(result.getContent());
            Integer resultInt = (Integer) result.getContent();
            assertEquals(resultInt, new Integer(4));
        }).build());
    }
    
    @Test
    public void testRoundRobin() {
        for (int outer = 0; outer < 3; outer++) {
            for (int inner = 1; inner < 6; inner++) {
                MessageBus.fire(ROUND_ROBIN_MESSAGE, inner);
            }
        }
    }
    
    @Test
    public void testRedeliveryCall() {
        MessageBus.addSubscription(ROUND_ROBIN_REDELIVERY, new CountDownReceiver(10));
        MessageBus.addSubscription(ROUND_ROBIN_REDELIVERY, new CountDownReceiver(0));
        MessageBus.fire(ROUND_ROBIN_REDELIVERY, MessageOptions.Builder.newInstance().deliveryCall((Callback) (ResponseHolder response) -> {
            assertTrue(response.getHeaders().containsKey(GlobalCons.G_EXCEPTION_HEADER));
        }, 2).build());
    }
    
    @Test
    public void emptyContextTest() {
        MessageHolder holder = new MessageHolder();
        String uid = MessageContextHolder.getContext().getTrxId();
        assertEquals(uid, holder.getTrxId());
    }
    
    @Test
    public void fullContextTest() {
        MessageContextHolder.setContext(new MessageContext("TEST"));
        MessageHolder holder = new MessageHolder();
        assertEquals("TEST", holder.getTrxId());
    }
    
    @After
    public void tearDown() {
        MessageBus.clearBus();
    }
    
}
