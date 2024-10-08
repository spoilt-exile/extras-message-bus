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

import tk.freaxsoftware.extras.bus.MessageHolder;
import tk.freaxsoftware.extras.bus.annotation.Receive;

/**
 *
 * @author spoilt
 */
public class TestReceiver {
    
    public static final String TEST_MESSAGE = "Org.Reflection.Test";
    
    public static final String TEST_MESSAGE_ERROR = "Org.Reflection.Test.Error";
    
    public static final String TEST_PATTERN_MESSAGE = "Org.Reflection.*";
    
    public volatile Boolean calledPattern = false;
    
    @Receive(TEST_MESSAGE)
    public void test(MessageHolder message) {
        message.getResponse().setContent(new Object());
    }
    
    @Receive(TEST_MESSAGE_ERROR)
    public void testError(Object content) {
        
    }
    
    @Receive(TEST_PATTERN_MESSAGE)
    public void testPattern(MessageHolder message) {
        calledPattern = true;
    }
    
}
