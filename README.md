MessageBus
============

Basic message bus service. Suport sync and async message delivery. Point-to-point 
messaging with round-robin and broadcasting. Message callbacks to delivery results 
of message processing (for point-to-point only). 

**Current version:** *5.0*

## Usage

To subscribe on message call method `addSubscription` with string as message destination and instance of the `Receiver` interface:

```java
MessageBus.addSubscription("Test.Message", (message) -> {
    System.out.printf("System message received %s", holder.getMessageId());
});
```

Bulk subscription possible by `addSubscriptions(stringArray, receiver)`

Each message contains:
 * `id` - unique id of the message;
 * `status` - status of message processing;
 * `topic` - message destination (address);
 * `headers` - string-to-string map for additional data;
 * `content` - any object (class payload should be familiar to server and subscriber);

Also message can hold response but it's only available to callback implementation. See below.

**To fire message:**

```java
//Sync, point-to-point, no callback, no ensure, no headers, string content
MessageBus.fire("Test.Empty", "SomeString");

//Sync, point-to-point, no callback, no ensure, headers, no content
MessageBus.fire("Test.Empty2", MessageOptions.Builder.newInstance().header("SomeHeader", "value").build());

//Async, point-to-point, no callback, no ensure, headers, Long content
MessageBus.fire("Test.Empty3", new Long(22), MessageOptions.Builder.newInstanc().header("SomeHeader", "value").async().build());

//Async, point-to-point, callback, no ensure, headers, Long content
MessageBus.fire("Test.Empty4", new Long(22), MessageOptions.Builder.newInstanc().header("SomeHeader", "value").async().callback((response) -> {
    System.out.println("Messsage callback after procession!")
}).build());
```

##### Available options for messages:
1. Sync or async mode - message may be processed in the same or in another thread. Sync mode will hang current thread.
2. Broadcast or point-to-point - message may be delivered for all subscribers or for just one (round-robin).
3. Callback - some logic which will be executed only after message processing. Success not guaranteed. Callback not available for broadcast messages.
4. Delivery policy - controls importance of the message: 
   - `VOID` (set by `deliveryVoid()` method in builder, **default**) - message for testing, bus will not take any action if there is no receivers; 
   - `CALL` (set by `deliveryCall()` method in builder) - message for calling remote service and getting response, time-sensetive, bus will throw message if there is no recievers;
   - `STORE` (set by `deliveryNotification()` in builder) - stores messages if there is no receivers at the moment and trying to send them in future;
5. Redelivery attempts counter - controls how many times message bus redelivery will try to send this message. After all attempts failed bus will store this message and will not try to redeliver it ever again. By default equals `3`;

`MessageOptions` instance could be reused.

## Config

Message bus will try to read `bus.json` config from resources folder.

Example:
```java
{
    "threadPoolConfig": { //Bus thread pool config;
        "type": "FIXED_POOL", //Type of the pool: SINGLE_POOL, CACHED_POOL, FIXED_POOL or FORK_JOIN_POOL;
        "threadCount": 8 //Number or workers (for async messaging);
    },
    "bridgeServer": { //HTTP bridge server config;
        "nested": false,  //Use nested SPARK JAVA server;
        "httpPort": 4444, //Server port (for own SPARK instance);
        "sparkThreadPoolMaxSize": 16 //Size of SPARK thread pool (for own SPARK instance);
        "heartbeatRate": 15 //Heart beat rate in seconds;
    },
    "bridgeClient": { //Config to establish connection to message bus server;
        "address": "127.0.0.1", //Address of the server;
        "port": 8080, //Port of the server;
        "heartbeatRate": 10, //Heart beat rate in seconds;
        "additionalSubscriptions": [ //Make additional subscriptions to send on server;
            "TEST",
            "TEST2",
        ]
    },
    "storage": { //Config for message storage and redelivery
        "storageClass": "org.test.MessageStorageImpl", //Path to class to storage implementation;
        "storageClassArgs": { //Key-value holder for data needed by strorage to work (optional);
            "dbName": "test1",
            "dbUser": "user",
            "dbPassword": "pass"
        }
        "redeliveryPeriod": 120, //Period in seconds between redelivery attempts;
        "topicPattern": "Test.*", //Pattern of topic to store messages;
        "storeCalls": true, //Store messages with delivery policy `CALL`;
        "removeProcessed": false //Removes processed messages;
    }
}
```

Central node should config only `bridgeServer` but other nodes should config both server and client. How it works: central node will establish server and listens for other node subscriptions, when mentioned event happens on central node it will be delivered on subscriber node via HTTP. In order to make node connection reliable also add `heartBeatRate` on central node server config and on subscriber node client config. Heart beat rate should be at lest slightly larger on server side. Node with obselete heart beat will be disconnected by force.

Bridge server and client config can be overrided by system properties if needed. Following properties available by now: `bridge.server.hearbeat`, `bridge.server.port`, `bridge.client.address` and `bridge.client.port`;

From 5.0 message bus supports storing messages for redelivery and logging. It provides interface `MessageStorage` for further implementation. There is only one built-in implementation of storage: `tk.freaxsoftware.extras.bus.storage.InMemoryMessageStorage` but it's not recommened for production use.

## Annotation driven receivers

It's possible to subscribe certain method of the class for several messages by using special `AnnotationUtil`.

**Recevier class:**
```java
public class OrderService {
    
    @Receive("Org.Test.CreateOrder")
    public void createOrder(MessageHolder<Order> orderMessage) {
        //logic here
    }
    
    @Receive("Org.Test.Delete")
    public void deleteOrder(MessageHolder<Order> orderMessage) {
        //logic here
    }
}
```

To subscribe:
```java
//Class subscription, instance will be created by annotation util
AnnotationUtil.subscribeReceiverClass(OrderService.class);

//Instance subscription
AnnotationUtil.subscribeReceiverInstance(new OrderService(dbConnection));
```

Also `AnnotationUtil` provides opposite methods to unsubscribe instances and classes.

## Copyright and license terms

Library distributed under terms of GNU LGPLv3 license.

**© Freax Software 2015-2017**