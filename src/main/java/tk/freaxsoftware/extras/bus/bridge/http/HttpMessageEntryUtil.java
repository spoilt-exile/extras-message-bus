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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.time.ZonedDateTime;
import java.util.Map;
import tk.freaxsoftware.extras.bus.MessageStatus;
import tk.freaxsoftware.extras.bus.bridge.http.util.GsonUtils;

/**
 * Http message entries factory. Used for deserialization.
 * @author Stanislav Nepochatov
 */
public class HttpMessageEntryUtil {
    
    private final Gson gson = GsonUtils.getGson();
    
    /**
     * Deseralize message entry from the json object.
     * @param object json object;
     * @return parsed http message entry;
     */
    public HttpMessageEntry deserialize(JsonObject object) throws ClassNotFoundException {
        String id = object.get("id").getAsString();
        String trxId = object.get("trxId").getAsString();
        String topic = object.get("topic").getAsString();
        ZonedDateTime created = ZonedDateTime.parse(object.get("created").getAsString());
        ZonedDateTime updated = object.has("updated") ? ZonedDateTime.parse(object.get("updated").getAsString()) : null;
        MessageStatus status = MessageStatus.valueOf(object.get("status").getAsString());
        Map<String, String> headers = gson.fromJson(object.get("headers"), new TypeToken<Map<String, String>>(){}.getType());
        HttpMessageEntry entry;
        TypeToken contentType = determineType(object);
        if (object.has("typeName")) {
            headers.put(LocalHttpCons.L_HTTP_NODE_REGISTERED_TYPE_HEADER, object.get("typeName").getAsString());
        }
        if (contentType != null) {
            Object content = gson.fromJson(object.get("content"), contentType.getType());
            entry = new HttpMessageEntry(id, trxId, created, updated, status, topic, headers, content);
        } else {
            entry = new HttpMessageEntry(id, trxId, created, updated, status, topic, headers, null);
        }
        return entry;
    }
    
    private TypeToken determineType(JsonObject object) throws ClassNotFoundException {
        if (object.has("typeName") && TypeResolver.isTypeRegistered(object.get("typeName").getAsString())) {
            return TypeResolver.resolveType(object.get("typeName").getAsString());
        } else if (object.has("fullTypeName")) {
            return TypeToken.get(Class.forName(object.get("fullTypeName").getAsString()));
        }
        return null;
    }
    
}
