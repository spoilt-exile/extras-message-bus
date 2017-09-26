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
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import tk.freaxsoftware.extras.bus.GlobalIds;
import tk.freaxsoftware.extras.bus.MessageHolder;

/**
 * Abstract HTTP sender implements single method for sending by http.
 * @author Stanislav Nepochatov
 */
public class AbstractHttpSender {
    
    private final HttpMessageEntryFactory messageFactory = new HttpMessageEntryFactory();
    
    private final HttpClient client = HttpClientBuilder.create().build();
    
    private final Gson gson = new Gson();
    
    protected HttpMessageEntry sendEntry(String address, Integer port, HttpMessageEntry entry) throws UnsupportedEncodingException, IOException, ClassNotFoundException, URISyntaxException {
        HttpPost request = new HttpPost(new URI("http", null, address, port, LocalHttpIds.LOCAL_HTTP_URL, null, null));
        request.setEntity(new StringEntity(gson.toJson(entry), ContentType.APPLICATION_JSON));
        HttpResponse response = client.execute(request);
        if (Objects.equals(LocalHttpIds.Mode.CALLBACK, entry.getHeaders().get(LocalHttpIds.LOCAL_HTTP_HEADER_MODE))) {
            if (response.getEntity() != null) {
                JsonObject bodyJson = new JsonParser().parse(new InputStreamReader(response.getEntity().getContent())).getAsJsonObject();
                HttpMessageEntry responseEntry = messageFactory.deserialize(bodyJson);
                return responseEntry;
            } else {
                throw new IllegalStateException(String.format("Node %s didn't return callback on message %s", address, entry.getMessageId()));
            }
        } else {
            if (response.getStatusLine().getStatusCode() > 400) {
                throw new IllegalStateException(String.format("Node %s returns error status %d on message %s", address, response.getStatusLine().getStatusCode(), entry.getMessageId()));
            }
        }
        return null;
    }
    
    protected void setupMessageMode(MessageHolder message, HttpMessageEntry entry) {
        if (message.getOptions().isBroadcast()) {
            entry.getHeaders().put(LocalHttpIds.LOCAL_HTTP_HEADER_MODE, LocalHttpIds.Mode.BROADCAST);
        } else if (message.getOptions().getCallback() != null) {
            entry.getHeaders().put(LocalHttpIds.LOCAL_HTTP_HEADER_MODE, LocalHttpIds.Mode.CALLBACK);
        } else {
            entry.getHeaders().put(LocalHttpIds.LOCAL_HTTP_HEADER_MODE, LocalHttpIds.Mode.SIMPLE);
        }
    }
}
