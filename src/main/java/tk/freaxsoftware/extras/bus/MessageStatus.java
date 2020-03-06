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
package tk.freaxsoftware.extras.bus;

/**
 * Status of the message.
 * @author Stanislav Nepochatov
 */
public enum MessageStatus {
    
    /**
     * Message just commited to the bus.
     */
    NEW,
    
    /**
     * Message delivered to receiver and processing already started.
     */
    PROCESSING,
    
    /**
     * Message processing is finished without errors.
     */
    FINISHED,
    
    /**
     * Message processing completed with error or not completed at all.
     */
    ERROR;
}
