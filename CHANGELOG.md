# [5.1] - 29.05.2020
 - Add `fireCall` method for direct sync calls;
 - Add handlers and callbacks for correct exception info transferring via http bridge;
 - Add filtration of the headers in http entity, it will filter out headers starts with `Trans` (transient);
 - Add logging of the exceptions on redelivery and grouping jobs;
 - Add new header `Global.Storage.Ignore` for storage to ignore messages with it;
 - Fix issues with storage by adding deserialization adapter for ZonedDateTime in GSON;
 - Fix bug in annotation tool when subscriber method use generic types;
 - Fix serialization of message mode in http by making them strings;
 - Fix sending http message mode;
 - Fix crossconnection init by subscribing to `Local.Http.Message.CrossNodeUp` ahead of sending `Local.Http.Message.CrossNode`;
 - Rethrow exception in reflect receiver, required for correct error handling;

# [5.0] - 14.04.2020
 - Add possibility to store messages through adapter;
 - Message redelivery after several failed attempts (see message options);
 - Grouping of messages to send several messages as list by timeout or on reaching size limit;
 - Cross connection between nodes;
 - Add unique id for message, transaction id and status;
 - Move to Java 11;
 - Massive refactor, see README;

# [4.2] - 08.05.2019
 - Fix overriding bridge.client.address property;

# [4.1] - 09.03.2018
 - Add additional subscriptions for client config: make server to subscribe on certain messages from subscriber;
 - Isolate loop notifications on server;
 - Use hearbeat to reinit connection after server downtime;
 - Fix NPE during async processing;
 - Reduce logging on hearbeat;

# [4.0] - 09.10.2017
 - Point-to-point message mode with round robin;
 - MessageBus configuration by JSON;
 - HTTP bridging for messages between several nodes;
 - Removing obselete code: ignition, several message fire methods;
 - Message options with builder;
 - Annotation driven receivers;

# [3.0] - 26.02.2016
 - Update version of `fast-storage`;

# [1.0] - 07.01.2016
 - Basic message bus implementation with `fast-storage` integration;
