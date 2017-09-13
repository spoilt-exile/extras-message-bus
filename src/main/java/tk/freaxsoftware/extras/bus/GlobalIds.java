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

/**
 * Global ids class provide name of global and wide used messages and data keys.
 * @author Stanislav Nepochatov
 */
public class GlobalIds {
    
    /**
     * Global notification on new subscriptions.
     */
    public static final String GLOBAL_SUBSCRIPTION = "Global.Subscription";
    
    /**
     * Global header for new subsciption id.
     */
    public static final String GLOBAL_HEADER_SUBSCRIPTION_ID = "Global.Headers.SubscriptionId";
    
    /**
     * Global header for exception class. May be overrided by last executed receiver.
     */
    public static final String GLOBAL_HEADER_EXCEPTION = "Global.Headers.Exception";
    
    /**
     * Id for result of simple messaging. Result is not an error.
     */
    public static final String GLOBAL_MESSAGE = "Global.Message";
    
    /**
     * Id of error message result.
     */
    public static final String GLOBAL_ERROR_MESSAGE = "Global.Error.Message";
}
