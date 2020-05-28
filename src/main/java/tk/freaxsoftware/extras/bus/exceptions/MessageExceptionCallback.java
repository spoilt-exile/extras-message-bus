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
 * Message exception callback allows to decrypt exception from response and 
 * rethrow it.
 * @author Stanislav Nepochatov
 */
public interface MessageExceptionCallback {
    
    /**
     * Detects if specified response contains error message and 
     * throw it as an exception.
     * @param response message response;
     * @throws java.lang.Exception if response hold error info;
     */
    void callback(ResponseHolder response) throws Exception;
    
}
