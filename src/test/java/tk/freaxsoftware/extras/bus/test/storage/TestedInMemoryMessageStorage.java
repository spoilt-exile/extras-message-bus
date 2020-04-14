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

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import tk.freaxsoftware.extras.bus.MessageHolder;
import tk.freaxsoftware.extras.bus.storage.InMemoryMessageStorage;

/**
 * In memory message storage for tests.
 * @author Stanislav Nepochatov
 * @since 5.0
 */
public class TestedInMemoryMessageStorage extends InMemoryMessageStorage {
    
    public static TestedInMemoryMessageStorage instance = null;
    
    public TestedInMemoryMessageStorage() {
        instance = this;
    }
    
    public Set<MessageHolder> getMessagesByTopic(String topic) {
        return storage.entrySet().stream()
                .filter(topicEntry -> Objects.equals(topicEntry.getValue().getTopic(), topic))
                .map(entry -> entry.getValue()).collect(Collectors.toSet());
    }
}
