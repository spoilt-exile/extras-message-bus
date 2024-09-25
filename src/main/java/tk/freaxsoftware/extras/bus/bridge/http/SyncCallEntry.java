/*
 * This file is part of MessageBus library.
 * 
 * Copyright (C) 2022 Freax Software
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

import tk.freaxsoftware.extras.bus.MessageStatus;

/**
 * Contains all data required to refresh status of message on caller node.
 * @author Stanislav Nepochatov
 */
public class SyncCallEntry {
    
    private String uuid;
    
    private MessageStatus status;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }
    
}
