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

import java.util.HashMap;
import java.util.Map;

/**
 * Message holder for important information related to message.
 * @author Stanislav Nepochatov
 * @param <T> type of message content;
 */
public class MessageHolder<T> {
    
    /**
     * Message id: destinantion of the message.
     */
    private String messageId;
    
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
    }

    /**
     * Detail constructor.
     * @param messageId id of the message;
     * @param options options of the message;
     * @param content content of the message;
     */
    public MessageHolder(String messageId, MessageOptions options, T content) {
        this();
        this.messageId = messageId;
        this.options = options;
        this.content = content;
        this.response = new ResponseHolder();
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
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
}
