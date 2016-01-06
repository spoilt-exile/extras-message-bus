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
package tk.freaxsoftware.extras.bus.test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import tk.freaxsoftware.extras.bus.Callback;
import tk.freaxsoftware.extras.bus.MessageBus;
import tk.freaxsoftware.extras.bus.ignition.MessageBusIgnition;
import tk.freaxsoftware.extras.faststorage.exception.EntityProcessingException;

/**
 * Checks ignition works.
 * @author Stanislav Nepochatov
 */
public class IgnitionTest {
    
    public IgnitionTest() {
    }
    
    @Before
    public void setUp() throws FileNotFoundException, EntityProcessingException {
        FileInputStream stream = new FileInputStream("messagebus.ign");
        MessageBusIgnition.ignite(stream);
    }
    
    @Test
    public void ignitionTest() {
        MessageBus.fireMessageSync("IGNITION_TEST_MESSAGE", null, new Callback() {

            @Override
            public void callback(Map<String, Object> result) {
                assertTrue((Boolean) result.get("result"));
            }
            
        });
    }
    
}
