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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import tk.freaxsoftware.extras.bus.MessageHolder;
import tk.freaxsoftware.extras.bus.MessageOptions;
import tk.freaxsoftware.extras.bus.bridge.http.util.GsonUtils;

/**
 * Abstract HTTP sender implements single method for sending by http.
 * @author Stanislav Nepochatov
 */
public abstract class AbstractHttpSender {
    
    /**
     * Message util instance.
     */
    private final HttpMessageEntryUtil messageUtil = new HttpMessageEntryUtil();
    
    /**
     * Http client instance.
     */
    private final HttpClientBuilder clientBuilder = HttpClientBuilder.create();
    
    /**
     * Gson instance.
     */
    private final Gson gson = GsonUtils.getGson();
    
    /**
     * Send message entry over HTTP to specified address and port.
     * @param address ip address or host;
     * @param port port number of HTTP server;
     * @param entry message entry to deliver;
     * @return response entry or null if there is no callback to return response;
     * @throws UnsupportedEncodingException
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws URISyntaxException 
     */
    protected HttpMessageEntry sendEntry(String address, Integer port, HttpMessageEntry entry) throws UnsupportedEncodingException, IOException, ClassNotFoundException, URISyntaxException {
        HttpPost request = new HttpPost(new URI("http", null, address, port, LocalHttpCons.L_HTTP_URL, null, null));
        request.setEntity(new StringEntity(gson.toJson(entry), ContentType.APPLICATION_JSON));
        HttpResponse response = clientBuilder.build().execute(request);
        if (Objects.equals(LocalHttpCons.Mode.CALLBACK.name(), entry.getHeaders().get(LocalHttpCons.L_HTTP_MODE_HEADER))) {
            if (response.getEntity() != null) {
                JsonObject bodyJson = new JsonParser().parse(new InputStreamReader(response.getEntity().getContent())).getAsJsonObject();
                HttpMessageEntry responseEntry = messageUtil.deserialize(bodyJson);
                return responseEntry;
            } else {
                throw new IllegalStateException(String.format("Node %s:%d didn't return callback on message %s", address, port, entry.getTopic()));
            }
        } else {
            if (response.getStatusLine().getStatusCode() > 400) {
                throw new IllegalStateException(String.format("Node %s:%d returns error status %d on message %s", address, port, response.getStatusLine().getStatusCode(), entry.getTopic()));
            }
        }
        return null;
    }
    
    /**
     * Setup mode of the message bridging.
     * @param message message holder;
     * @param entry entry to send over HTTP;
     */
    protected void setupMessageMode(MessageHolder message, HttpMessageEntry entry) {
        if (message.getOptions().isBroadcast()) {
            entry.getHeaders().put(LocalHttpCons.L_HTTP_MODE_HEADER, LocalHttpCons.Mode.BROADCAST.name());
        } else if (message.getOptions().getCallback() != null 
                || message.getOptions().getDeliveryPolicy() == MessageOptions.DeliveryPolicy.CALL) {
            entry.getHeaders().put(LocalHttpCons.L_HTTP_MODE_HEADER, LocalHttpCons.Mode.CALLBACK.name());
        } else {
            entry.getHeaders().put(LocalHttpCons.L_HTTP_MODE_HEADER, LocalHttpCons.Mode.ASYNC.name());
        }
    }
}
