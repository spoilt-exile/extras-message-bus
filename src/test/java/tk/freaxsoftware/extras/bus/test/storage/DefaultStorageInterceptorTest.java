/*
 * This file is part of MessageBus library.
 * 
 * Copyright (C) 2020 Freax Software
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
package tk.freaxsoftware.extras.bus.test.storage;

import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;
import tk.freaxsoftware.extras.bus.MessageBus;
import tk.freaxsoftware.extras.bus.MessageHolder;
import tk.freaxsoftware.extras.bus.MessageOptions;

/**
 * Tests storage cases.
 * @author Stanislav Nepochatov
 * @since 5.0
 */
public class DefaultStorageInterceptorTest {
    
    private static final String STORE_TOPIC = "Store.Test";
    private static final String STORE_CALL_TOPIC = "Store.Call";
    private static final String STORE_GROUP_TOPIC_SINGLE = "Store.Group.Single";
    private static final String STORE_GROUP_TOPIC_LIST = "Store.Group.List";
    
    private static final MessageOptions syncNotify = MessageOptions.Builder.newInstance().deliveryNotification().sync().build();
    
    @Test
    public void storeTest() {
        MessageHolder holder = new MessageHolder(STORE_TOPIC, syncNotify, null);
        MessageBus.fire(holder);
        Set<MessageHolder> messages = TestedInMemoryMessageStorage.instance.getUnprocessedMessagesByTopic(STORE_TOPIC);
        assertFalse(messages.isEmpty());
        MessageHolder stored = messages.iterator().next();
        assertEquals(stored.getId(), holder.getId());
        TestedInMemoryMessageStorage.instance.removeMessage(holder.getId());
        Set<MessageHolder> messages2 = TestedInMemoryMessageStorage.instance.getUnprocessedMessagesByTopic(STORE_TOPIC);
        assertTrue(messages2.isEmpty());
    }
    
    @Test
    public void storeCallTest() {
        MessageHolder holder = new MessageHolder(STORE_CALL_TOPIC, MessageOptions.callOptions(null, null), null);
        MessageBus.addSubscription(STORE_CALL_TOPIC, (message) -> {});
        MessageBus.fire(holder);
        Set<MessageHolder> messages = TestedInMemoryMessageStorage.instance.getMessagesByTopic(STORE_CALL_TOPIC);
        assertFalse(messages.isEmpty());
        MessageHolder stored = messages.iterator().next();
        assertEquals(stored.getId(), holder.getId());
        TestedInMemoryMessageStorage.instance.removeMessage(holder.getId());
        Set<MessageHolder> messages2 = TestedInMemoryMessageStorage.instance.getMessagesByTopic(STORE_CALL_TOPIC);
        assertTrue(messages2.isEmpty());
    }
    
    @Test
    public void storeVoidTest() {
        MessageHolder holder = new MessageHolder(STORE_CALL_TOPIC, MessageOptions.defaultOptions(null), null);
        MessageBus.addSubscription(STORE_CALL_TOPIC, (message) -> {});
        MessageBus.fire(holder);
        Set<MessageHolder> messages = TestedInMemoryMessageStorage.instance.getMessagesByTopic(STORE_CALL_TOPIC);
        assertTrue(messages.isEmpty());
    }
    
    @Test
    public void groupingTest() {
        MessageBus.addSubscription(STORE_GROUP_TOPIC_LIST, (message) -> {});
        MessageBus.fire(new MessageHolder(STORE_GROUP_TOPIC_SINGLE, syncNotify, new Object()));
        MessageBus.fire(new MessageHolder(STORE_GROUP_TOPIC_SINGLE, syncNotify, new Object()));
        MessageBus.fire(new MessageHolder(STORE_GROUP_TOPIC_SINGLE, syncNotify, new Object()));
        MessageBus.fire(new MessageHolder(STORE_GROUP_TOPIC_SINGLE, syncNotify, new Object()));
        Set<MessageHolder> messages = TestedInMemoryMessageStorage.instance.getMessagesByTopic(STORE_GROUP_TOPIC_LIST);
        assertTrue(messages.isEmpty());
        
        MessageBus.fire(new MessageHolder(STORE_GROUP_TOPIC_SINGLE, syncNotify, new Object()));
        Set<MessageHolder> messages2 = TestedInMemoryMessageStorage.instance.getMessagesByTopic(STORE_GROUP_TOPIC_LIST);
        assertFalse(messages2.isEmpty());
    }
}
