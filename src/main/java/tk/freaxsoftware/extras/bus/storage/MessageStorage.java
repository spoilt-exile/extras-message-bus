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

import java.util.Set;
import tk.freaxsoftware.extras.bus.MessageHolder;

/**
 * Stores messages to process them later.
 * @author Stanislav Nepochatov
 */
public interface MessageStorage {
    
    /**
     * Saves message to storage with key.
     * @param message unprocessed message;
     * @param key id of the receiver which is temporary unavailable (may be null);
     */
    void saveMessage(MessageHolder message, String key);
    
    /**
     * Get set of messages by message id (topic).
     * @param messageId id to search;
     * @return set of messages;
     */
    Set<MessageHolder> getMessages(String messageId);
    
    /**
     * Get set of messages by message id and key.
     * @param messageId id to search;
     * @param key id of the receiver;
     * @return set of messages;
     */
    Set<MessageHolder> getMessagesByKey(String messageId, String key);
    
}
