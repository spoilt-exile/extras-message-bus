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
package tk.freaxsoftware.extras.bus.exceptions;

import tk.freaxsoftware.extras.bus.ResponseHolder;

/**
 * Exception services allows to override message bus behavior during
 * error handling.
 * @author Stanislav Nepochatov
 */
public class ExceptionServices {
    
    /**
     * Message exception handler.
     */
    private static MessageExceptionHandler handler;
    
    /**
     * Message exception callback.
     */
    private static MessageExceptionCallback callback;
    
    /**
     * Handles current error by packing exception info in response.
     * @param response message response;
     * @param ex exception;
     */
    public static void handle(ResponseHolder response, Exception ex) {
        if (handler != null) {
            handler.handle(response, ex);
        }
    }
    
    /**
     * Receives error info by message and rethrow it as an exception;
     * @param response message response;
     * @throws Exception exception to be thrown if response is an error;
     */
    public static void callback(ResponseHolder response) throws Exception {
        if (callback != null) {
            callback.callback(response);
        }
    }
    
    /**
     * Register message exception handler.
     * @param newHandler handler to register;
     */
    public static void registerHandler(MessageExceptionHandler newHandler) {
        handler = newHandler;
    }
    
    /**
     * Register message exception callback.
     * @param newCallback callback to register;
     */
    public static void registerCallback(MessageExceptionCallback newCallback) {
        callback = newCallback;
    }
}
