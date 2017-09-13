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

/**
 * Message receiver interface.
 * @author Stanislav Nepochatov
 * @param <T>
 */
public interface Receiver<T> {
    
    /**
     * Receive message with arguments, process it and place results.
     * @param message message holder instance;
     * @throws Exception receiver may throws any exception;
     */
    void receive(MessageHolder<T> message) throws Exception;
    
}
