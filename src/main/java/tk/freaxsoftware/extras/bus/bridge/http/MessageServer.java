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
import com.google.gson.JsonParser;
import spark.Request;
import spark.Response;
import static spark.Spark.*;
import tk.freaxsoftware.extras.bus.MessageBus;
import tk.freaxsoftware.extras.bus.MessageOptions;
import tk.freaxsoftware.extras.bus.config.http.ServerConfig;

/**
 * Message server endpoint.
 * @author Stanislav Nepochatov
 */
public class MessageServer {
    
    private final HttpMessageEntryFactory messageFactory = new HttpMessageEntryFactory();
    
    private final Gson gson = new Gson();
    
    public void init(ServerConfig config) {
        threadPool(config.getSparkThreadPoolMaxSize());
        port(config.getHttpPort());
        
        post("/broker/message", "application/json", (Request req, Response res) -> {
            JsonObject bodyJson = new JsonParser().parse(req.body()).getAsJsonObject();
            HttpMessageEntry entry = messageFactory.deserialize(bodyJson);
            LocalHttpIds.Mode mode = LocalHttpIds.Mode.valueOf((String) entry.getHeaders().getOrDefault(LocalHttpIds.LOCAL_HTTP_HEADER_CALLBACK, LocalHttpIds.Mode.SIMPLE.name()));
            HttpMessageEntry response = new HttpMessageEntry();
            switch (mode) {
                case BROADCAST:
                    MessageBus.fire(entry.getMessageId(), entry.getContent(), entry.getHeaders(), MessageOptions.Builder.newInstance().broadcast().build());
                    break;
                case CALLBACK:
                    MessageBus.fire(entry.getMessageId(), entry.getContent(), entry.getHeaders(), MessageOptions.Builder.newInstance().callback((messageResponse) -> {
                        response.setMessageId(entry.getMessageId());
                        response.setHeaders(messageResponse.getHeaders());
                        if (messageResponse.getContent() != null) {
                            response.setFullTypeName(messageResponse.getContent().getClass().getCanonicalName());
                            response.setTypeName(messageResponse.getContent().getClass().getSimpleName());
                            response.setContent(messageResponse.getContent());
                        }
                    }).build());
                    break;
                default:
                    MessageBus.fire(entry.getMessageId(), entry.getContent(), entry.getHeaders(), MessageOptions.Builder.newInstance().async().build());
            }
            if (response.getMessageId() != null) {
                return response;
            } else {
                res.status(200);
                return "";
            }
        }, gson::toJson);
    }
    
}