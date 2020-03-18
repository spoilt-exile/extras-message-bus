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
package tk.freaxsoftware.extras.bus.storage;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import tk.freaxsoftware.extras.bus.MessageHolder;
import tk.freaxsoftware.extras.bus.MessageStatus;

/**
 * In-memory implementation of the message storage.
 * @author Stanislav Nepochatov
 * @since 5.0
 */
public class InMemoryMessageStorage implements MessageStorage {
    
    private final Map<String, MessageHolder> storage = new ConcurrentHashMap<>();

    @Override
    public void saveMessage(MessageHolder message) {
        storage.put(message.getId(), message);
    }

    @Override
    public Set<MessageHolder> getUnprocessedMessages() {
        return storage.entrySet().stream()
                .filter(entry -> entry.getValue().getStatus() == MessageStatus.ERROR)
                .map(entry -> entry.getValue()).collect(Collectors.toSet());
    }

    @Override
    public void removeMessage(String id) {
        storage.remove(id);
    }
    
}
