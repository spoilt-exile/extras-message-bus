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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.javalin.Javalin;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.freaxsoftware.extras.bus.MessageBus;
import tk.freaxsoftware.extras.bus.MessageContext;
import tk.freaxsoftware.extras.bus.MessageContextHolder;
import tk.freaxsoftware.extras.bus.MessageHolder;
import tk.freaxsoftware.extras.bus.MessageOptions;
import tk.freaxsoftware.extras.bus.MessageStatus;
import tk.freaxsoftware.extras.bus.bridge.http.util.GsonMapper;
import tk.freaxsoftware.extras.bus.bridge.http.util.GsonUtils;
import tk.freaxsoftware.extras.bus.config.http.ServerConfig;
import tk.freaxsoftware.extras.bus.storage.StorageInterceptor;

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
     * Deploy spark endpoint for message listening. It will config spark if config not nested.
     * @param config server config;
     * @param interceptor message storage interceptor;
     */
    public void init(ServerConfig config, StorageInterceptor interceptor) {
        LOGGER.info(String.format("Deploying new HTTP server on port %d", config.getHttpPort()));
        Javalin app = Javalin.create(javalinConfig -> {
            javalinConfig.jsonMapper(new GsonMapper());
        }).start(config.getHttpPort());
        
        app.post(LocalHttpCons.L_HTTP_URL, ctx -> {
            JsonObject bodyJson = new JsonParser().parse(ctx.body()).getAsJsonObject();
            HttpMessageEntry entry = messageUtil.deserialize(bodyJson);
            MessageContextHolder.setContext(new MessageContext(entry.getTrxId()));
            entry.getHeaders().put(LocalHttpCons.L_HTTP_NODE_IP_HEADER, ctx.ip());
            LocalHttpCons.Mode mode = LocalHttpCons.Mode.valueOf((String) entry.getHeaders().getOrDefault(LocalHttpCons.L_HTTP_MODE_HEADER, LocalHttpCons.Mode.ASYNC.name()));
            HttpMessageEntry response = new HttpMessageEntry();
            MessageOptions options;
            MessageHolder holder = entry.toMessageHolder();
            switch (mode) {
                case BROADCAST:
                    options = MessageOptions.Builder.newInstance().deliveryNotification().broadcast().headers(entry.getHeaders()).build();
                    holder.setRedeliveryCounter(options.getRedeliveryCounter());
                    break;
                case CALLBACK:
                    options = MessageOptions.Builder.newInstance().deliveryCall().headers(entry.getHeaders()).callback((messageResponse) -> {
                        response.initAsResponse(holder, messageResponse);
                    }).build();
                    break;
                default:
                    if (entry.getHeaders().containsKey(LocalHttpCons.L_HTTP_NODE_SYNC_CALL_HEADER)) {
                        options = MessageOptions.Builder.newInstance().async().headers(entry.getHeaders()).callback(new SyncCallback(entry.getHeaders(), entry.getId())).build();
                    } else {
                        options = MessageOptions.Builder.newInstance().async().headers(entry.getHeaders()).build();
                    }
            }
            holder.setOptions(options);
            MessageBus.fire(holder);
            if (response.getTopic() != null) {
                ctx.json(response);
            } else {
                ctx.status(200);
            }
        });
        
        app.post(LocalHttpCons.L_HTTP_SYNC_URL, ctx -> {
            SyncCallEntry syncCall = GsonUtils.getGson().fromJson(ctx.body(), SyncCallEntry.class);
            Optional<MessageHolder> messageOpt = interceptor.getStorage().getMessageById(syncCall.getUuid());
            messageOpt.ifPresent(mh -> {
                mh.setStatus(syncCall.getStatus());
                if (mh.getStatus() == MessageStatus.FINISHED) {
                    interceptor.storeProcessedMessage(mh);
                } else {
                    interceptor.storeMessage(mh);
                }
            });
            ctx.status(200);
        });
    }
    
}
