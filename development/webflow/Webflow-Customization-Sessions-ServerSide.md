# Server-side Sessions

Source: https://apereo.github.io/cas/development/webflow/Webflow-Customization-Sessions-ServerSide.html

In the event that you wish to use server-side session storage for managing the webflow session, you will need to enable this behavior via CAS properties.

The following settings and properties are available from the CAS configuration catalog:

- `cas.webflow.session.storage` (Required; type `Boolean`; default `false`): Controls whether spring webflow sessions are to be stored server-side or client side. By default state is managed on the client side, that is also signed and encrypted.
- `cas.webflow.session.pin-to-session` (Optional; type `Boolean`; default `false`): Controls whether the webflow session is pinned to the client's IP address and user-agent.
- `cas.webflow.session.server.compress` (Optional; type `Boolean`; default `false`): Whether or not the snapshots should be compressed. Only relevant if session storage is done on the server.
- `cas.webflow.session.server.lock-timeout` (Optional; type `String`; default `PT30S`): Sets the time period that can elapse before a timeout occurs on an attempt to acquire a conversation lock. The default is 30 seconds. Only relevant if session storage is done on the server.
- `cas.webflow.session.server.max-conversations` (Optional; type `Integer`; default `5`): Using the maxConversations property, you can limit the number of concurrently active conversations allowed in a single session. If the maximum is exceeded, the conversation manager will automatically end the oldest conversation. The default is 5, which should be fine for most situations. Set it to -1 for no limit. Setting maxConversations to 1 allows easy resource cleanup in situations where there should only be one active conversation per session. Only relevant if session storage is done on the server.
- `spring.session.servlet.filter-dispatcher-types` (Third party; type `Set<DispatcherType>`; default `["async", "error", "request"]`): Session repository filter dispatcher types.
- `spring.session.servlet.filter-order` (Third party; type `Integer`): Session repository filter order.
- `spring.session.timeout` (Third party; type `Duration`): Session timeout. If a duration suffix is not specified, seconds will be used.

Doing so will likely require you to also enable sticky sessions and/or session replication in a clustered deployment of CAS.

> **Usage Warning!**
>
> Generally speaking, you do not need to enable server-side sessions unless you have a rather specialized deployment or are in need of features that store bits and pieces of data into a sever-backed session object. It is recommended that you stick with the default client-side session storage and only switch if and when mandated by a specific CAS behavior.

## Storage Options

If you do wish to use server-side session storage, CAS supports the following options for storing webflow sessions:

| Topic | Description |
| --- | --- |
| Hazelcast | [See this guide](https://apereo.github.io/cas/development/webflow/Webflow-Customization-Sessions-ServerSide-Hazelcast.html). |
| JDBC | [See this guide](https://apereo.github.io/cas/development/webflow/Webflow-Customization-Sessions-ServerSide-JDBC.html). |
| MongoDb | [See this guide](https://apereo.github.io/cas/development/webflow/Webflow-Customization-Sessions-ServerSide-MongoDb.html). |
| Redis | [See this guide](https://apereo.github.io/cas/development/webflow/Webflow-Customization-Sessions-ServerSide-Redis.html). |
| Ticket Registry | [See this guide](https://apereo.github.io/cas/development/webflow/Webflow-Customization-Sessions-ServerSide-TicketRegistry.html). |
