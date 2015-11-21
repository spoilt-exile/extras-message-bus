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

import java.util.Map;

/**
 * Message receiver interface.
 * @author Stanislav Nepochatov
 */
public interface Receiver {
    
    /**
     * Receive message with arguments, process it and place results.
     * @param messageId message id string;
     * @param arguments arguments passed with message dispatch;
     * @param result message processing result which may be returned to peer;
     * @throws Exception receiver may throws any exception;
     */
    void receive(String messageId, Map<String, Object> arguments, Map<String, Object> result) throws Exception;
    
}
