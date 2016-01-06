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
package tk.freaxsoftware.extras.bus.ignition;

import org.slf4j.Logger;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import org.slf4j.LoggerFactory;
import tk.freaxsoftware.extras.bus.MessageBus;
import tk.freaxsoftware.extras.bus.Receiver;
import tk.freaxsoftware.extras.faststorage.exception.EntityProcessingException;
import tk.freaxsoftware.extras.faststorage.reading.EntityStreamReader;
import tk.freaxsoftware.extras.faststorage.reading.EntityStreamReaderImpl;

/**
 * Main library ignition class for easy receiver registration.
 * @author spoilt
 */
public class MessageBusIgnition {
    
    private static final Logger logger = LoggerFactory.getLogger(MessageBusIgnition.class);
    
    /**
     * Reads receivers's info from stream and init them with registration.
     * @param descriptorStream stream of resource with ECSV list of handler enties;
     * @throws EntityProcessingException 
     */
    public static void ignite(InputStream descriptorStream) throws EntityProcessingException {
        Reader reader = new InputStreamReader(descriptorStream);
        logger.warn("Processing ignition...");
        EntityStreamReader<ReceiverEntry> entityReader = new EntityStreamReaderImpl<>(ReceiverEntry.class, ReceiverEntry.DEFINITION);
        List<ReceiverEntry> receiversList = entityReader.readEntities(reader);
        for (ReceiverEntry receiverEntry: receiversList) {
            logger.warn("Processing entry: " + receiverEntry);
            Receiver receiver = igniteReceiver(receiverEntry);
            if (receiver != null) {
                String[] subscriptions = receiverEntry.getMessageSubscriptions().toArray(new String[receiverEntry.getMessageSubscriptions().size()]);
                MessageBus.addSubscriptions(subscriptions, receiver);
            }
        }
    }
    
    /**
     * Creates receiver instance based on class name from entry.
     * @param entry receiver entry;
     * @return reciver instance or null;
     */
    private static Receiver igniteReceiver(ReceiverEntry entry) {
        Class receiverClass = null;
        Receiver receiver = null;
        try {
            receiverClass = Class.forName(entry.getReceiverClass());
        } catch (ClassNotFoundException clex) {
            logger.error("Can't find class: " + entry.getReceiverClass());
        }
        if (receiverClass != null) {
            try {
                receiver = (Receiver) receiverClass.newInstance();
            } catch (InstantiationException | IllegalAccessException insex) {
                logger.error("Class " + entry.getReceiverClass() + " can't be instance due absent public constructor or else", insex);
            } catch (ClassCastException clex) {
                logger.error("Class " + entry.getReceiverClass() + " can't be casted to Receiver instance.", clex);
            }
        }
        return receiver;
    }
    
}
