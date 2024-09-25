/*
 * This file is part of MessageBus library.
 * 
 * Copyright (C) 2019 Freax Software
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

import java.util.Optional;
import java.util.Set;
import tk.freaxsoftware.extras.bus.MessageHolder;

/**
 * Stores messages to process them later.
 * @author Stanislav Nepochatov
 * @since 5.0
 */
public interface MessageStorage {
    
    /**
     * Saves message to storage with key.
     * @param message unprocessed message;
     */
    void saveMessage(MessageHolder message);
    
    /**
     * Get set of messages for further processing.
     * @return set of messages;
     */
    Set<MessageHolder> getUnprocessedMessages();
    
    /**
     * Get set of message for further processing by topic.
     * @param topic topic to search;
     * @return set of the messages by topic;
     */
    Set<MessageHolder> getUnprocessedMessagesByTopic(String topic);
    
    /**
     * Get set of message for grouping by topic.
     * @param topic topic to search;
     * @return set of the messages by topic;
     */
    Set<MessageHolder> getGroupingMessagesByTopic(String topic);
    
    /**
     * Get message by id.
     * @param id unique id of the message;
     * @return optional of the message;
     */
    Optional<MessageHolder> getMessageById(String id);
    
    /**
     * Removes message from the storage.
     * @param id of the message to delete;
     */
    void removeMessage(String id);
}
