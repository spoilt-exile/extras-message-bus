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

package tk.freaxsoftware.extras.bus;

import java.util.Iterator;
import java.util.List;

/**
 * Round robin iterator for point-to-point message processing balancing.
 * @author Stanislav Nepochatov
 */
public class RoundRobinIterator<T> implements Iterator<T> {
    
    /**
     * List of the items.
     */
    private final List<T> list;
    
    /**
     * Index of item.
     */
    private int index = 0;

    /**
     * Default constructor.
     * @param list collection of the items;
     */
    public RoundRobinIterator(List<T> list) {
        this.list = list;
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public T next() {
        T res = list.get(index);
        index = (index + 1) % list.size();
        return res;
    }
    
}
