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
package tk.freaxsoftware.extras.bus.test.bridge.http;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import tk.freaxsoftware.extras.bus.MessageStatus;
import tk.freaxsoftware.extras.bus.bridge.http.HttpMessageEntry;
import tk.freaxsoftware.extras.bus.bridge.http.HttpMessageEntryUtil;
import tk.freaxsoftware.extras.bus.bridge.http.TypeResolver;
import tk.freaxsoftware.extras.bus.bridge.http.util.GsonUtils;

/**
 * Testing message type resolving, serialization and deserialization.
 * @author Stanislav Nepochatov
 */
public class HttpMessageEntryUtilTest {
    
    private final static String ENTRY_TYPE_NAME = "MapOfEntry";
    
    private final static String FULL_TYPE_MESSAGE = "message_with_fulltype.json";
    private final static String REG_TYPE_MESSAGE = "message_with_reg_type.json";
    
    private HttpMessageEntryUtil util = new HttpMessageEntryUtil();
    
    @Test
    public void deserializationTestByClass() throws Exception {
        JsonObject json = JsonParser.parseReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(FULL_TYPE_MESSAGE))).getAsJsonObject();
        HttpMessageEntry entry = util.deserialize(json);
        Assert.assertNotNull(entry.getId());
        Assert.assertNotNull(entry.getTrxId());
        Assert.assertNotNull(entry.getCreated());
        Assert.assertNotNull(entry.getHeaders());
        Assert.assertTrue(entry.getHeaders().size() == 1);
        Assert.assertEquals(entry.getStatus(), MessageStatus.PROCESSING);
        Assert.assertNotNull(entry.getHeaders());
        Assert.assertEquals(entry.getFullTypeName(), "java.math.BigDecimal");
        Assert.assertEquals(entry.getContent(), BigDecimal.ONE);
        Assert.assertNull(entry.getTypeName());
    }
    
    @Test
    public void deserializationTestByRegType() throws Exception {
        TypeResolver.register(ENTRY_TYPE_NAME, new TypeToken<HashMap<String, Entry>>() {});
        JsonObject json = JsonParser.parseReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(REG_TYPE_MESSAGE))).getAsJsonObject();
        HttpMessageEntry<Map<String, Entry>> entry = util.deserialize(json);
        Assert.assertNotNull(entry.getId());
        Assert.assertNotNull(entry.getTrxId());
        Assert.assertNotNull(entry.getCreated());
        Assert.assertNotNull(entry.getHeaders());
        Assert.assertTrue(entry.getHeaders().size() == 2);
        Assert.assertEquals(entry.getStatus(), MessageStatus.PROCESSING);
        Assert.assertNotNull(entry.getHeaders());
        Assert.assertEquals(entry.getTypeName(), ENTRY_TYPE_NAME);
        Assert.assertEquals(entry.getContent().size(), 3);
        Assert.assertEquals(entry.getContent().get("Item1"), new Entry("Cat1", 3.14f));
        Assert.assertEquals(entry.getContent().get("Item2"), new Entry("Cat1", 60.25f));
        Assert.assertEquals(entry.getContent().get("Item3"), new Entry("Cat2", 13.45f));
    }
    
    @Test
    @Ignore
    public void serializationTestByClass() {
        HttpMessageEntry entry = new HttpMessageEntry();
        entry.setCreated(ZonedDateTime.now());
        entry.setTypeName(ENTRY_TYPE_NAME);
        entry.setHeaders(new HashMap());
        entry.getHeaders().put("Header", "Test");
        entry.setId(UUID.randomUUID().toString());
        entry.setTrxId(UUID.randomUUID().toString());
        entry.setStatus(MessageStatus.PROCESSING);
        entry.setTopic("Message.Topic.Test");
        Map<String, Entry> mapOfEntry = new HashMap();
        mapOfEntry.put("Item1", new Entry("Cat1", 3.14f));
        mapOfEntry.put("Item2", new Entry("Cat1", 60.25f));
        mapOfEntry.put("Item3", new Entry("Cat2", 13.45f));
        entry.setContent(mapOfEntry);
        entry.setUpdated(ZonedDateTime.now());
        String json = GsonUtils.getGson().toJson(entry);
        System.out.println(json);
    }
    
    public static class Entry {
        
        private String category;
        private Float value;

        public Entry(String category, Float value) {
            this.category = category;
            this.value = value;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public Float getValue() {
            return value;
        }

        public void setValue(Float value) {
            this.value = value;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + Objects.hashCode(this.category);
            hash = 97 * hash + Objects.hashCode(this.value);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Entry other = (Entry) obj;
            if (!Objects.equals(this.category, other.category)) {
                return false;
            }
            if (!Objects.equals(this.value, other.value)) {
                return false;
            }
            return true;
        }
    }
    
}
