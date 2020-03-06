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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageServer.class);
    
    /**
     * Message util.
     */
    private final HttpMessageEntryUtil messageUtil = new HttpMessageEntryUtil();
    
    /**
     * Gson instance.
     */
    private final Gson gson = new Gson();
    
    /**
     * Deploy spark endpoint for message listening. It will config spark if config not nested.
     * @param config server config;
     */
    public void init(ServerConfig config) {
        if (!config.isNested()) {
            LOGGER.info(String.format("Deploying new HTTP server on port %d", config.getHttpPort()));
            threadPool(config.getSparkThreadPoolMaxSize());
            port(config.getHttpPort());
        } else {
            LOGGER.info("Using nested Spark instance.");
        }
        
        post(LocalHttpCons.L_HTTP_URL, "application/json", (Request req, Response res) -> {
            JsonObject bodyJson = new JsonParser().parse(req.body()).getAsJsonObject();
            HttpMessageEntry entry = messageUtil.deserialize(bodyJson);
            entry.getHeaders().put(LocalHttpCons.L_HTTP_NODE_IP_HEADER, req.ip());
            LocalHttpCons.Mode mode = LocalHttpCons.Mode.valueOf((String) entry.getHeaders().getOrDefault(LocalHttpCons.L_HTTP_MODE_HEADER, LocalHttpCons.Mode.ASYNC.name()));
            HttpMessageEntry response = new HttpMessageEntry();
            switch (mode) {
                case BROADCAST:
                    MessageBus.fire(entry.getTopic(), entry.getContent(), MessageOptions.Builder.newInstance().async().broadcast().headers(entry.getHeaders()).build());
                    break;
                case CALLBACK:
                    MessageBus.fire(entry.getTopic(), entry.getContent(), MessageOptions.Builder.newInstance().headers(entry.getHeaders()).callback((messageResponse) -> {
                        response.setTopic(entry.getTopic());
                        response.setHeaders(messageResponse.getHeaders());
                        if (messageResponse.getContent() != null) {
                            response.setFullTypeName(messageResponse.getContent().getClass().getCanonicalName());
                            response.setTypeName(messageResponse.getContent().getClass().getSimpleName());
                            response.setContent(messageResponse.getContent());
                        }
                    }).build());
                    break;
                default:
                    MessageBus.fire(entry.getTopic(), entry.getContent(), MessageOptions.Builder.newInstance().async().headers(entry.getHeaders()).build());
            }
            if (response.getTopic() != null) {
                return response;
            } else {
                res.status(200);
                return "";
            }
        }, gson::toJson);
    }
    
}
