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

package tk.freaxsoftware.extras.bus.bridge.http;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import tk.freaxsoftware.extras.bus.MessageHolder;
import tk.freaxsoftware.extras.bus.MessageStatus;
import tk.freaxsoftware.extras.bus.ResponseHolder;

/**
 * HTTP message entry. Used for incoming message and it's response.
 * @author Stanislav Nepochatov
 * @param <T> message content type;
 */
public class HttpMessageEntry<T> {
    
    /**
     * Prefix for transient header which shouldn't be transmitted by http bridging.
     */
    public final static String TRANSIENT_HEADER_PREFIX = "Trans";
    
    /**
     * Unique id of the message.
     */
    private String id;
    
    /**
     * Unique id of the transaction.
     */
    private String trxId;
    
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
     * Message destination.
     */
    private String topic;
    
    /**
     * Headers of the message.
     */
    private Map<String, String> headers;
    
    /**
     * Full name of the content class. If null content will be ignored.
     */
    private String fullTypeName;
    
    /**
     * Simple typename of the content class.
     */
    private String typeName;
    
    /**
     * Content of the message.
     */
    private T content;
    
    /**
     * Default constructor.
     */
    public HttpMessageEntry() {}
    
    protected HttpMessageEntry(MessageHolder<T> holder) {
        this.id = holder.getId();
        this.trxId = holder.getTrxId();
        this.created = holder.getCreated();
        this.updated = holder.getUpdated();
        this.status = holder.getStatus();
        this.topic = holder.getTopic();
        this.headers = filterHeaders(holder.getHeaders());
        this.content = holder.getContent();
        if (this.content != null) {
            this.fullTypeName = this.content.getClass().getCanonicalName();
            this.typeName = this.content.getClass().getSimpleName();
        }
    }

    /**
     * Full parametrick constructor for deserialization.
     * @param id id of message;
     * @param trxId id of transaction;
     * @param created date of message creation;
     * @param updated date of message last update;
     * @param status status of message;
     * @param topic message topic;
     * @param headers message headers (key-value map);
     * @param content content of the message;
     */
    public HttpMessageEntry(String id, String trxId, ZonedDateTime created, ZonedDateTime updated, MessageStatus status, String topic, Map<String, String> headers, T content) {
        this.id = id;
        this.trxId = trxId;
        this.created = created;
        this.updated = updated;
        this.status = status;
        this.topic = topic;
        this.headers = filterHeaders(headers);
        this.content = content;
        if (this.content != null) {
            this.fullTypeName = this.content.getClass().getCanonicalName();
            this.typeName = this.content.getClass().getSimpleName();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTrxId() {
        return trxId;
    }

    public void setTrxId(String trxId) {
        this.trxId = trxId;
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

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = filterHeaders(headers);
    }

    public String getFullTypeName() {
        return fullTypeName;
    }

    public void setFullTypeName(String fullTypeName) {
        this.fullTypeName = fullTypeName;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + Objects.hashCode(this.id);
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
        final HttpMessageEntry<?> other = (HttpMessageEntry<?>) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }
    
    public MessageHolder<T> toMessageHolder() {
        MessageHolder<T> holder = new MessageHolder<>();
        holder.setId(this.getId());
        holder.setTrxId(this.getTrxId());
        holder.setCreated(this.getCreated());
        holder.setUpdated(this.getUpdated());
        holder.setStatus(this.getStatus());
        holder.setTopic(this.getTopic());
        holder.setHeaders(this.getHeaders());
        holder.setContent(this.getContent());
        holder.setResponse(new ResponseHolder());
        return holder;
    }
    
    public void initAsResponse(MessageHolder holder, ResponseHolder<T> response) {
        this.id = holder.getId();
        this.trxId = holder.getTrxId();
        this.created = holder.getCreated();
        this.updated = holder.getUpdated();
        this.status = holder.getStatus();
        this.topic = holder.getTopic();
        this.headers = filterHeaders(response.getHeaders());
        this.content = response.getContent();
        if (this.content != null) {
            this.fullTypeName = this.content.getClass().getCanonicalName();
            this.typeName = this.content.getClass().getSimpleName();
        }
    }
    
    public Map<String, String> filterHeaders(Map<String, String> rawHeaders) {
        return rawHeaders.entrySet().stream()
                .filter(en -> !en.getKey().startsWith(TRANSIENT_HEADER_PREFIX))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
