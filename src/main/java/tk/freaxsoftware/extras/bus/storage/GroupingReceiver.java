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

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.freaxsoftware.extras.bus.MessageBus;
import tk.freaxsoftware.extras.bus.MessageHolder;
import tk.freaxsoftware.extras.bus.MessageOptions;
import tk.freaxsoftware.extras.bus.Receiver;

/**
 * Message receiver to group messages into one packet.
 * @author Stanislav Nepochatov
 * @since 5.0
*/
public class GroupingReceiver implements Receiver {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupingReceiver.class);
    
    private final MessageOptions options = MessageOptions.Builder.newInstance().async().deliveryNotification().build();
    
    private final GroupEntry configEntry;
    
    private final MessageStorage storage;
    
    private ZonedDateTime firstMessageCreated;
    
    private final Object firstMessageCreatedLock = new Object();

    public GroupingReceiver(GroupEntry configEntry, MessageStorage storage) {
        this.configEntry = configEntry;
        this.storage = storage;
    }

    @Override
    public void receive(MessageHolder message) throws Exception {
        synchronized(firstMessageCreatedLock) {
            if (firstMessageCreated == null) {
                firstMessageCreated = message.getCreated();
            }
        }
        Set<MessageHolder> unprocessed = storage.getUnprocessedMessagesByTopic(configEntry.getTopicSingle());
        if (unprocessed.size() > configEntry.getMaxSize()) {
            LOGGER.info("Start sending grouped messages to {} since max size of {} exceeded.", 
                    configEntry.getTopicList(), configEntry.getMaxSize());
            internalSend(unprocessed);
        }
    }
    
    public void sendMessagesByTimeout() {
        ZonedDateTime now = ZonedDateTime.now();
        if (ChronoUnit.SECONDS.between(firstMessageCreated, now) > configEntry.getMaxTimeInQueue()) {
            LOGGER.info("Start sending grouped messages to {} since max time in queue of {} passed.", 
                    configEntry.getTopicList(), configEntry.getMaxTimeInQueue());
            internalSend(storage.getUnprocessedMessagesByTopic(configEntry.getTopicSingle()));
        }
    }
    
    private void internalSend(Set<MessageHolder> unprocessed) {
        List contentList = unprocessed.stream().map(ms -> ms.getContent()).collect(Collectors.toList());
        MessageBus.fire(configEntry.getTopicList(), contentList, options);
        unprocessed.forEach((holder) -> {
            storage.removeMessage(holder.getId());
        });
        synchronized(firstMessageCreatedLock) {
            firstMessageCreated = null;
        }
        LOGGER.info("Sending of group event to {} complete, reseting state.", configEntry.getTopicList());
    }
    
}
