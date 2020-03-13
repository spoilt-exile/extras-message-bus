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
 * Global constants class provide name of global and wide used messages and data keys.
 * @author Stanislav Nepochatov
 */
public class GlobalCons {
    
    /**
     * Global notification on new subscriptions.
     */
    public static final String G_SUBSCRIBE_TOPIC = "Global.Subscribe";
    
    /**
     * Global notification on subscriptions cancelation.
     */
    public static final String G_UNSUBSCRIBE_TOPIC = "Global.Unsubscribe";
    
    /**
     * Global header for new subsciption topic.
     */
    public static final String G_SUBSCRIPTION_DEST_HEADER = "Global.Headers.SubscriptionTopic";
    
    /**
     * Global header for exception class. May be overrided by last executed receiver.
     */
    public static final String G_EXCEPTION_HEADER = "Global.Headers.Exception";
    
    /**
     * Global header for exception message. May be overrided by last executed receiver.
     */
    public static final String G_EXCEPTION_MESSAGE_HEADER = "Global.Headers.ExceptionMessage";
}
