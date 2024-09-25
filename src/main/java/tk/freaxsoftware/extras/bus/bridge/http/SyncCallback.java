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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.freaxsoftware.extras.bus.Callback;
import tk.freaxsoftware.extras.bus.MessageBus;
import tk.freaxsoftware.extras.bus.MessageStatus;
import tk.freaxsoftware.extras.bus.ResponseHolder;
import tk.freaxsoftware.extras.bus.bridge.http.util.GsonUtils;

/**
 * Special callback to sync state of message accros nodes.
 * @author Stanislav Nepochatov
 */
public class SyncCallback implements Callback<Object> {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(SyncCallback.class);
    
    private final String address;
    
    private final int port;
    
    private final String id;
    
    /**
     * Http client instance.
     */
    private final HttpClientBuilder clientBuilder = HttpClientBuilder.create();

    public SyncCallback(Map<String, Object> headers, String id) {
        this.address = (String) headers.get(LocalHttpCons.L_HTTP_NODE_IP_HEADER);
        this.port = Integer.parseInt((String) headers.getOrDefault(LocalHttpCons.L_HTTP_NODE_PORT_HEADER, "7000"));
        this.id = id;
    }

    @Override
    public void callback(ResponseHolder response) {
        MessageStatus status = MessageBus.isSuccessful(response.getHeaders()) ? MessageStatus.FINISHED : MessageStatus.ERROR;
        SyncCallEntry syncCallEntry = new SyncCallEntry();
        syncCallEntry.setStatus(status);
        syncCallEntry.setUuid(id);
        try {
            sendCallback(syncCallEntry);
        } catch (Exception ex) {
            LOGGER.error("Error during sending sync call for message {} with status {} to {} port {}", status, id, address, port);
            LOGGER.error("Details:", ex);
        }
    }
    
    private void sendCallback(SyncCallEntry entry) throws URISyntaxException, IOException {
        HttpPost request = new HttpPost(new URI("http", null, address, port, LocalHttpCons.L_HTTP_SYNC_URL, null, null));
        request.setEntity(new StringEntity(GsonUtils.getGson().toJson(entry), ContentType.APPLICATION_JSON));
        clientBuilder.build().execute(request);
    }
    
}
