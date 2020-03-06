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
package tk.freaxsoftware.extras.bus;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Message holder for important information related to message.
 * @author Stanislav Nepochatov
 * @param <T> type of message content;
 */
public class MessageHolder<T> {
    
    /**
     * Unique id of the message.
     */
    private String id;
    
    /**
     * Date of message creation.
     */
    private ZonedDateTime created;
    
    /**
     * Date of the last message update.
     */
    private ZonedDateTime updated;
    
    /**
     * Status of the message.
     */
    private MessageStatus status;
    
    /**
     * Topic of the message.
     */
    private String topic;
    
    /**
     * Options provided to message bus during sending of the message.
     */
    private MessageOptions options;
    
    /**
     * Headers of the message.
     */
    private Map<String, String> headers;
    
    /**
     * Content of the message.
     */
    private T content;
    
    /**
     * Response structure of the message.
     */
    private ResponseHolder response;
    
    /**
     * Default constructor.
     */
    public MessageHolder() {
        this.headers = new HashMap<>();
        this.id = UUID.randomUUID().toString();
        this.created = ZonedDateTime.now();
        this.status = MessageStatus.NEW;
    }

    /**
     * Detail constructor.
     * @param topic destination of the message;
     * @param options options of the message;
     * @param content content of the message;
     */
    public MessageHolder(String topic, MessageOptions options, T content) {
        this();
        this.topic = topic;
        this.options = options;
        this.content = content;
        this.response = new ResponseHolder();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ZonedDateTime getCreated() {
        return created;
    }

    public void setCreated(ZonedDateTime created) {
        this.created = created;
    }

    public ZonedDateTime getUpdated() {
        return updated;
    }

    public void setUpdated(ZonedDateTime updated) {
        this.updated = updated;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public MessageOptions getOptions() {
        return options;
    }

    public void setOptions(MessageOptions options) {
        this.options = options;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }

    public ResponseHolder getResponse() {
        return response;
    }

    public void setResponse(ResponseHolder response) {
        this.response = response;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MessageHolder<?> other = (MessageHolder<?>) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }
}
