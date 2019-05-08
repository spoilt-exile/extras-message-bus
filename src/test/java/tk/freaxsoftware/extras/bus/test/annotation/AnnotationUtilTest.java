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
package tk.freaxsoftware.extras.bus.test.annotation;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import spark.utils.Assert;
import tk.freaxsoftware.extras.bus.MessageBus;
import tk.freaxsoftware.extras.bus.MessageOptions;
import tk.freaxsoftware.extras.bus.annotation.AnnotationUtil;
import tk.freaxsoftware.extras.bus.exceptions.NoSubscriptionMessageException;

/**
 * Unit test for the annotation based receivers.
 * @author Stanislav Nepochatov
 */
public class AnnotationUtilTest {
    
    public AnnotationUtilTest() {
    }
    
    @Before
    public void setUp() throws InstantiationException, IllegalAccessException {
        AnnotationUtil.subscribeReceiverClass(TestReceiver.class);
    }
    
    @After
    public void tearDown() {
        AnnotationUtil.unsubscribeReceiverClass(TestReceiver.class);
        MessageBus.clearBus();
    }
    
    @Test
    public void testMessage() {
        MessageBus.fire(TestReceiver.TEST_MESSAGE, null, null, MessageOptions.Builder.newInstance().deliveryCall().callback((res) -> {
            Assert.notNull(res.getContent());
        }).build());
    }
    
    @Test(expected = NoSubscriptionMessageException.class)
    public void testMessageError() {
        MessageBus.fire(TestReceiver.TEST_MESSAGE_ERROR, null, null, MessageOptions.Builder.newInstance().deliveryCall().callback((res) -> {
            Assert.notNull(res.getContent());
        }).build());
    }
}
