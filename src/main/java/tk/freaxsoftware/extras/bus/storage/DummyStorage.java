/*
 * This file is part of MessageBus library.
 * 
 * Copyright (C) 2022 Freax Software
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

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import tk.freaxsoftware.extras.bus.MessageHolder;

/**
 * Dummy message storage to keep compatibility.
 * @author Stanislav Nepochatov
 */
public class DummyStorage implements MessageStorage {

    @Override
    public void saveMessage(MessageHolder message) {
        //Do nothing;
    }

    @Override
    public Set<MessageHolder> getUnprocessedMessages() {
        return Collections.EMPTY_SET;
    }

    @Override
    public Set<MessageHolder> getUnprocessedMessagesByTopic(String topic) {
        return Collections.EMPTY_SET;
    }

    @Override
    public Set<MessageHolder> getGroupingMessagesByTopic(String topic) {
        return Collections.EMPTY_SET;
    }

    @Override
    public Optional<MessageHolder> getMessageById(String id) {
        return Optional.empty();
    }

    @Override
    public void removeMessage(String id) {
        //Do nothing;
    }
    
}
