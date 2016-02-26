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

import java.util.List;
import tk.freaxsoftware.extras.faststorage.generic.ECSVAble;
import tk.freaxsoftware.extras.faststorage.generic.ECSVDefinition;
import tk.freaxsoftware.extras.faststorage.generic.ECSVFields;
import tk.freaxsoftware.extras.faststorage.reading.EntityReader;
import tk.freaxsoftware.extras.faststorage.writing.EntityWriter;

/**
 * Receiver description entry.
 * @author Stanislav Nepochatov
 */
public class ReceiverEntry implements ECSVAble<String>{
    
    public static final String TYPE = "RECEIVER";
    
    public static final ECSVDefinition DEFINITION = ECSVDefinition.createNew()
            .addPrimitive(ECSVFields.PR_STRING)
            .addArray(null);
    
    /**
     * Class name of receiver implementation.
     */
    private String receiverClass;
    
    /**
     * List of message ids which supports this receiver.
     */
    private List<String> messageSubscriptions;

    public String getReceiverClass() {
        return receiverClass;
    }

    public void setReceiverClass(String receiverClass) {
        this.receiverClass = receiverClass;
    }

    public List<String> getMessageSubscriptions() {
        return messageSubscriptions;
    }

    public void setMessageSubscriptions(List<String> messageSubscriptions) {
        this.messageSubscriptions = messageSubscriptions;
    }

    @Override
    public String getKey() {
        return receiverClass;
    }

    @Override
    public void setKey(String key) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public ECSVDefinition getDefinition() {
        return DEFINITION;
    }

    @Override
    public void readFromECSV(EntityReader<String> reader) {
        receiverClass = reader.readString();
        messageSubscriptions = reader.readArray();
    }

    @Override
    public void writeToECSV(EntityWriter<String> writer) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void update(ECSVAble<String> updatedEntity) {
        throw new UnsupportedOperationException("Not supported.");
    }
    
    @Override
    public String getEntityType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return "ReceiverEntry{" + "receiverClass=" + receiverClass + ", messageSubscriptions=" + messageSubscriptions + '}';
    }
}
