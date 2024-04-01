/*
 * This file is part of MessageBus library.
 * 
 * Copyright (C) 2024 Freax Software
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
package tk.freaxsoftware.extras.bus;

import java.util.function.Consumer;

/**
 * Receiver based on consumer.
 * @author Stanislav Nepochatov
 */
public class ConsumerReceiver <T> implements Receiver<T> {
    
    private final Consumer<MessageHolder<T>> consumer;

    public ConsumerReceiver(Consumer<MessageHolder<T>> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void receive(MessageHolder<T> message) throws Exception {
        consumer.accept(message);
    }
    
    public static <T> void subscribe(String topic, Consumer<MessageHolder<T>> consumer) {
        ConsumerReceiver<T> receiver = new ConsumerReceiver(consumer);
        MessageBus.addSubscription(topic, receiver);
    }
    
}
