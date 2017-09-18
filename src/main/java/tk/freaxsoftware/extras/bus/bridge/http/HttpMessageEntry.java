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

import java.util.Map;

/**
 * HTTP message entry. Used for incoming message and it's response.
 * @author Stanislav Nepochatov
 * @param <T> message content type;
 */
public class HttpMessageEntry<T> {
    
    private String messageId;
    
    private Map<String, String> headers;
    
    private String fullTypeName;
    
    private String typeName;
    
    private T content;
    
    public HttpMessageEntry() {}

    public HttpMessageEntry(String messageId, Map<String, String> headers, T content) {
        this.messageId = messageId;
        this.headers = headers;
        this.content = content;
        if (this.content != null) {
            this.fullTypeName = this.content.getClass().getCanonicalName();
            this.typeName = this.content.getClass().getSimpleName();
        }
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
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
    
}
