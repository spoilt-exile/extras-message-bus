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
package tk.freaxsoftware.extras.bus.bridge.http.util;

import com.google.gson.Gson;
import io.javalin.plugin.json.JsonMapper;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import org.apache.commons.io.IOUtils;

/**
 * GSON mapper for Javalin.
 * @author Stanislav Nepochatov
 */
public class GsonMapper implements JsonMapper {
    
    /**
     * Gson instance.
     */
    private final Gson gson = GsonUtils.getGson();

    @Override
    public String toJsonString(Object obj) {
        return gson.toJson(obj);
    }

    @Override
    public InputStream toJsonStream(Object obj) {
        return IOUtils.toInputStream(gson.toJson(obj), Charset.defaultCharset());
    }

    @Override
    public <T> T fromJsonString(String json, Class<T> targetClass) {
        return gson.fromJson(json, targetClass);
    }

    @Override
    public <T> T fromJsonStream(InputStream json, Class<T> targetClass) {
        return gson.fromJson(new InputStreamReader(json), targetClass);
    }
    
}
