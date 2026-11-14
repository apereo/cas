# Configuration Management - Clustered Deployments with AMQP

Source: https://apereo.github.io/cas/development/configuration/Configuration-Management-Clustered-AMQP.html

This is one option for broadcasting change events to CAS nodes. [RabbitMQ](https://www.rabbitmq.com/) is open source message broker software (sometimes called message-oriented middleware) that implements the Advanced Message Queuing Protocol (AMQP).

Support is enabled by including the following dependency in the final overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-configuration-cloud-amqp"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `spring.rabbitmq.address-shuffle-mode` (Third party; type `AbstractConnectionFactory.AddressShuffleMode`; default `none`): Mode used to shuffle configured addresses.
- `spring.rabbitmq.addresses` (Third party; type `List<String>`): List of addresses to which the client should connect. When set, the host and port are ignored.
- `spring.rabbitmq.cache.channel.checkout-timeout` (Third party; type `Duration`): Duration to wait to obtain a channel if the cache size has been reached. If 0, always create a new channel.
- `spring.rabbitmq.cache.channel.size` (Third party; type `Integer`): Number of channels to retain in the cache. When "check-timeout" > 0, max channels per connection.
- `spring.rabbitmq.cache.connection.mode` (Third party; type `CachingConnectionFactory.CacheMode`; default `channel`): Connection factory cache mode.
- `spring.rabbitmq.cache.connection.size` (Third party; type `Integer`): Number of connections to cache. Only applies when mode is CONNECTION.
- `spring.rabbitmq.channel-rpc-timeout` (Third party; type `Duration`; default `10m`): Continuation timeout for RPC calls in channels. Set it to zero to wait forever.
- `spring.rabbitmq.connection-timeout` (Third party; type `Duration`): Connection timeout. Set it to zero to wait forever.
- `spring.rabbitmq.dynamic` (Third party; type `Boolean`; default `true`): Whether to create an AmqpAdmin bean.
- `spring.rabbitmq.host` (Third party; type `String`; default `localhost`): RabbitMQ host. Ignored if an address is set.
- `spring.rabbitmq.listener.direct.acknowledge-mode` (Third party; type `AcknowledgeMode`): Acknowledge mode of container.
- `spring.rabbitmq.listener.direct.auto-startup` (Third party; type `Boolean`; default `true`): Whether to start the container automatically on startup.
- `spring.rabbitmq.listener.direct.consumers-per-queue` (Third party; type `Integer`): Number of consumers per queue.
- `spring.rabbitmq.listener.direct.de-batching-enabled` (Third party; type `Boolean`; default `true`): Whether the container should present batched messages as discrete messages or call the listener with the batch.
- `spring.rabbitmq.listener.direct.default-requeue-rejected` (Third party; type `Boolean`): Whether rejected deliveries are re-queued by default.
- `spring.rabbitmq.listener.direct.force-stop` (Third party; type `Boolean`; default `false`): Whether the container (when stopped) should stop immediately after processing the current message or stop after processing all pre-fetched messages.
- `spring.rabbitmq.listener.direct.idle-event-interval` (Third party; type `Duration`): How often idle container events should be published.
- `spring.rabbitmq.listener.direct.missing-queues-fatal` (Third party; type `Boolean`; default `false`): Whether to fail if the queues declared by the container are not available on the broker.
- `spring.rabbitmq.listener.direct.observation-enabled` (Third party; type `Boolean`; default `false`): Whether to enable observation.
- `spring.rabbitmq.listener.direct.prefetch` (Third party; type `Integer`): Maximum number of unacknowledged messages that can be outstanding at each consumer.
- `spring.rabbitmq.listener.direct.retry.enabled` (Third party; type `Boolean`; default `false`): Whether publishing retries are enabled.
- `spring.rabbitmq.listener.direct.retry.initial-interval` (Third party; type `Duration`; default `1000ms`): Duration between the first and second attempt to deliver a message.
- `spring.rabbitmq.listener.direct.retry.max-attempts` (Third party; type `Long`; deprecation `ERROR`, replaced by `spring.rabbitmq.listener.direct.retry.max-retries`)
- `spring.rabbitmq.listener.direct.retry.max-interval` (Third party; type `Duration`; default `10000ms`): Maximum duration between attempts.
- `spring.rabbitmq.listener.direct.retry.max-retries` (Third party; type `Long`; default `3`): Maximum number of retry attempts to deliver a message.
- `spring.rabbitmq.listener.direct.retry.multiplier` (Third party; type `Double`; default `1`): Multiplier to apply to the previous retry interval.
- `spring.rabbitmq.listener.direct.retry.stateless` (Third party; type `Boolean`; default `true`): Whether retries are stateless or stateful.
- `spring.rabbitmq.listener.simple.acknowledge-mode` (Third party; type `AcknowledgeMode`): Acknowledge mode of container.
- `spring.rabbitmq.listener.simple.auto-startup` (Third party; type `Boolean`; default `true`): Whether to start the container automatically on startup.
- `spring.rabbitmq.listener.simple.batch-size` (Third party; type `Integer`): Batch size, expressed as the number of physical messages, to be used by the container.
- `spring.rabbitmq.listener.simple.concurrency` (Third party; type `Integer`): Minimum number of listener invoker threads.
- `spring.rabbitmq.listener.simple.consumer-batch-enabled` (Third party; type `Boolean`; default `false`): Whether the container creates a batch of messages based on the 'receive-timeout' and 'batch-size'. Coerces 'de-batching-enabled' to true to include the contents of a producer created batch in the batch as discrete records.
- `spring.rabbitmq.listener.simple.de-batching-enabled` (Third party; type `Boolean`; default `true`): Whether the container should present batched messages as discrete messages or call the listener with the batch.
- `spring.rabbitmq.listener.simple.default-requeue-rejected` (Third party; type `Boolean`): Whether rejected deliveries are re-queued by default.
- `spring.rabbitmq.listener.simple.force-stop` (Third party; type `Boolean`; default `false`): Whether the container (when stopped) should stop immediately after processing the current message or stop after processing all pre-fetched messages.
- `spring.rabbitmq.listener.simple.idle-event-interval` (Third party; type `Duration`): How often idle container events should be published.
- `spring.rabbitmq.listener.simple.max-concurrency` (Third party; type `Integer`): Maximum number of listener invoker threads.
- `spring.rabbitmq.listener.simple.missing-queues-fatal` (Third party; type `Boolean`; default `true`): Whether to fail if the queues declared by the container are not available on the broker and/or whether to stop the container if one or more queues are deleted at runtime.
- `spring.rabbitmq.listener.simple.observation-enabled` (Third party; type `Boolean`; default `false`): Whether to enable observation.
- `spring.rabbitmq.listener.simple.prefetch` (Third party; type `Integer`): Maximum number of unacknowledged messages that can be outstanding at each consumer.
- `spring.rabbitmq.listener.simple.retry.enabled` (Third party; type `Boolean`; default `false`): Whether publishing retries are enabled.
- `spring.rabbitmq.listener.simple.retry.initial-interval` (Third party; type `Duration`; default `1000ms`): Duration between the first and second attempt to deliver a message.
- `spring.rabbitmq.listener.simple.retry.max-attempts` (Third party; type `Long`; deprecation `ERROR`, replaced by `spring.rabbitmq.listener.simple.retry.max-retries`)
- `spring.rabbitmq.listener.simple.retry.max-interval` (Third party; type `Duration`; default `10000ms`): Maximum duration between attempts.
- `spring.rabbitmq.listener.simple.retry.max-retries` (Third party; type `Long`; default `3`): Maximum number of retry attempts to deliver a message.
- `spring.rabbitmq.listener.simple.retry.multiplier` (Third party; type `Double`; default `1`): Multiplier to apply to the previous retry interval.
- `spring.rabbitmq.listener.simple.retry.stateless` (Third party; type `Boolean`; default `true`): Whether retries are stateless or stateful.
- `spring.rabbitmq.listener.simple.transaction-size` (Third party; type `Integer`; deprecation `ERROR`, no replacement)
- `spring.rabbitmq.listener.stream.native-listener` (Third party; type `Boolean`; default `false`): Whether the container will support listeners that consume native stream messages instead of Spring AMQP messages.
- `spring.rabbitmq.listener.stream.observation-enabled` (Third party; type `Boolean`; default `false`): Whether to enable observation.
- `spring.rabbitmq.listener.type` (Third party; type `RabbitProperties.ContainerType`; default `simple`): Listener container type.
- `spring.rabbitmq.max-inbound-message-body-size` (Third party; type `DataSize`; default `64MB`): Maximum size of the body of inbound (received) messages.
- `spring.rabbitmq.password` (Third party; type `String`; default `guest`): Login to authenticate against the broker.
- `spring.rabbitmq.port` (Third party; type `Integer`): RabbitMQ port. Ignored if an address is set. Default to 5672, or 5671 if SSL is enabled.
- `spring.rabbitmq.publisher-confirm-type` (Third party; type `CachingConnectionFactory.ConfirmType`): Type of publisher confirms to use.
- `spring.rabbitmq.publisher-confirms` (Third party; type `Boolean`; deprecation `ERROR`, no replacement)
- `spring.rabbitmq.publisher-returns` (Third party; type `Boolean`; default `false`): Whether to enable publisher returns.
- `spring.rabbitmq.requested-channel-max` (Third party; type `Integer`; default `2047`): Number of channels per connection requested by the client. Use 0 for unlimited.
- `spring.rabbitmq.requested-heartbeat` (Third party; type `Duration`): Requested heartbeat timeout; zero for none. If a duration suffix is not specified, seconds will be used.
- `spring.rabbitmq.ssl.algorithm` (Third party; type `String`): SSL algorithm to use. By default, configured by the Rabbit client library.
- `spring.rabbitmq.ssl.bundle` (Third party; type `String`): SSL bundle name.
- `spring.rabbitmq.ssl.enabled` (Third party; type `Boolean`): Whether to enable SSL support. Determined automatically if an address is provided with the protocol (amqp:// vs. amqps://).
- `spring.rabbitmq.ssl.key-store` (Third party; type `String`): Path to the key store that holds the SSL certificate.
- `spring.rabbitmq.ssl.key-store-algorithm` (Third party; type `String`; default `SunX509`): Key store algorithm.
- `spring.rabbitmq.ssl.key-store-password` (Third party; type `String`): Password used to access the key store.
- `spring.rabbitmq.ssl.key-store-type` (Third party; type `String`; default `PKCS12`): Key store type.
- `spring.rabbitmq.ssl.trust-store` (Third party; type `String`): Trust store that holds SSL certificates.
- `spring.rabbitmq.ssl.trust-store-algorithm` (Third party; type `String`; default `SunX509`): Trust store algorithm.
- `spring.rabbitmq.ssl.trust-store-password` (Third party; type `String`): Password used to access the trust store.
- `spring.rabbitmq.ssl.trust-store-type` (Third party; type `String`; default `JKS`): Trust store type.
- `spring.rabbitmq.ssl.validate-server-certificate` (Third party; type `Boolean`; default `true`): Whether to enable server side certificate validation.
- `spring.rabbitmq.ssl.verify-hostname` (Third party; type `Boolean`; deprecation `WARNING`, no replacement; default `true`): Whether to enable hostname verification.
- `spring.rabbitmq.stream.host` (Third party; type `String`; default `localhost`): Host of a RabbitMQ instance with the Stream plugin enabled.
- `spring.rabbitmq.stream.name` (Third party; type `String`): Name of the stream.
- `spring.rabbitmq.stream.password` (Third party; type `String`): Login password to authenticate to the broker. When not set spring.rabbitmq.password is used.
- `spring.rabbitmq.stream.port` (Third party; type `Integer`): Stream port of a RabbitMQ instance with the Stream plugin enabled.
- `spring.rabbitmq.stream.ssl.bundle` (Third party; type `String`): SSL bundle name.
- `spring.rabbitmq.stream.ssl.enabled` (Third party; type `Boolean`): Whether to enable SSL support. Enabled automatically if "bundle" is provided.
- `spring.rabbitmq.stream.username` (Third party; type `String`): Login user to authenticate to the broker. When not set, spring.rabbitmq.username is used.
- `spring.rabbitmq.stream.virtual-host` (Third party; type `String`): Virtual host of a RabbitMQ instance with the Stream plugin enabled. When not set, spring.rabbitmq.virtual-host is used.
- `spring.rabbitmq.template.allowed-list-patterns` (Third party; type `List<String>`): Simple patterns for allowable packages/classes for deserialization.
- `spring.rabbitmq.template.default-receive-queue` (Third party; type `String`): Name of the default queue to receive messages from when none is specified explicitly.
- `spring.rabbitmq.template.exchange` (Third party; type `String`): Name of the default exchange to use for send operations.
- `spring.rabbitmq.template.mandatory` (Third party; type `Boolean`): Whether to enable mandatory messages.
- `spring.rabbitmq.template.observation-enabled` (Third party; type `Boolean`; default `false`): Whether to enable observation.
- `spring.rabbitmq.template.queue` (Third party; type `String`; deprecation `ERROR`, replaced by `spring.rabbitmq.template.default-receive-queue`)
- `spring.rabbitmq.template.receive-timeout` (Third party; type `Duration`): Timeout for receive() operations.
- `spring.rabbitmq.template.reply-timeout` (Third party; type `Duration`): Timeout for sendAndReceive() operations.
- `spring.rabbitmq.template.retry.enabled` (Third party; type `Boolean`; default `false`): Whether publishing retries are enabled.
- `spring.rabbitmq.template.retry.initial-interval` (Third party; type `Duration`; default `1000ms`): Duration between the first and second attempt to deliver a message.
- `spring.rabbitmq.template.retry.max-attempts` (Third party; type `Long`; deprecation `ERROR`, replaced by `spring.rabbitmq.template.retry.max-retries`)
- `spring.rabbitmq.template.retry.max-interval` (Third party; type `Duration`; default `10000ms`): Maximum duration between attempts.
- `spring.rabbitmq.template.retry.max-retries` (Third party; type `Long`; default `3`): Maximum number of retry attempts to deliver a message.
- `spring.rabbitmq.template.retry.multiplier` (Third party; type `Double`; default `1`): Multiplier to apply to the previous retry interval.
- `spring.rabbitmq.template.routing-key` (Third party; type `String`): Value of a default routing key to use for send operations.
- `spring.rabbitmq.username` (Third party; type `String`; default `guest`): Login user to authenticate to the broker.
- `spring.rabbitmq.virtual-host` (Third party; type `String`): Virtual host to use when connecting to the broker.

## Troubleshooting

To enable additional logging, modify the logging configuration file to add the following:

```xml
<Logger name="org.springframework.amqp" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>
```
