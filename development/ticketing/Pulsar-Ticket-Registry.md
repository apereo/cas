# Apache Pulsar Ticket Registry

Source: https://apereo.github.io/cas/development/ticketing/Pulsar-Ticket-Registry.html

The Apache Pulsar ticket registry is very much an extension of the [default ticket registry](https://apereo.github.io/cas/development/ticketing/Default-Ticket-Registry.html). The difference is that ticket operations applied to the registry are broadcasted using Pulsar topics to other listening CAS nodes. Each node keeps copies of ticket state on its own and only instructs others to keep their copy accurate by broadcasting messages and data associated with each. Each message and ticket registry instance running inside a CAS node in the cluster is tagged with a unique identifier in order to avoid endless looping behavior and recursive needless inbound operations.

The broadcast and pub/sub mechanism is backed by [Apache Pulsar](https://pulsar.apache.org/). Apache Pulsar is an open-source, distributed messaging and streaming platform built for the cloud.

In Apache Pulsar, topics do not need to be created beforehand. They are created automatically on first use, as long as:

- The namespace exists, and
- Auto-creation is enabled in the broker configuration (it is enabled by default in most local/docker setups).

Every CAS node must receive every change, so each node subscribes to these topics under a subscription of its own, named after the configured subscription name followed by the node’s queue identifier. Subscriptions are durable in Pulsar: give every node a stable queue identifier, otherwise each restart leaves an abandoned subscription behind for which the broker keeps retaining messages.

The following settings and properties are available from the CAS configuration catalog:

- `cas.ticket.registry.core.queue-identifier` (Optional; type `String`): Identifier for this CAS server node that tags the sender/receiver in the queue and avoid processing of inbound calls. If left blank, an identifier is generated automatically and kept in memory.

Support is enabled by including the following dependency in the overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-pulsar-ticket-registry"
}
```

## CAS Configuration

The following settings and properties are available from the CAS configuration catalog:

- `cas.ticket.registry.in-memory.crypto.encryption.key` (Required; type `String`): The encryption key. The encryption key by default and unless specified otherwise must be randomly-generated string whose length is defined by the encryption key size setting.
- `cas.ticket.registry.in-memory.crypto.signing.key` (Required; type `String`): The signing key is a string whose length is defined by the signing key size setting.
- `cas.ticket.registry.in-memory.concurrency` (Optional; type `Integer`; default `20`): The estimated number of concurrently updating threads. The implementation performs internal sizing to try to accommodate this many threads.
- `cas.ticket.registry.in-memory.crypto.alg` (Optional; type `String`; default `AES`): The signing/encryption algorithm to use.
- `cas.ticket.registry.in-memory.crypto.enabled` (Optional; type `Boolean`; default `true`): Whether crypto operations are enabled.
- `cas.ticket.registry.in-memory.crypto.encryption.key-size` (Optional; type `Integer`; default `16`): Encryption key size.
- `cas.ticket.registry.in-memory.crypto.signing-enabled` (Optional; type `Boolean`; default `true`): Whether signing encryption operations are enabled.
- `cas.ticket.registry.in-memory.crypto.signing.key-size` (Optional; type `Integer`; default `512`): The signing key size.
- `cas.ticket.registry.in-memory.initial-capacity` (Optional; type `Integer`; default `1000`): The initial capacity of the underlying memory store. The implementation performs internal sizing to accommodate this many elements.
- `cas.ticket.registry.in-memory.load-factor` (Optional; type `Integer`; default `1`): The load factor threshold, used to control resizing. Resizing may be performed when the average number of elements per bin exceeds this threshold.
- `cas.ticket.registry.in-memory.properties` (Optional; type `Map<String,String>`)
- `cas.ticket.registry.pulsar.concurrency` (Optional; type `Integer`; default `1`): Number of consumers that receive ticket registry messages concurrently on each node.
- `cas.ticket.registry.pulsar.subscription-name` (Optional; type `String`; default `cas-pulsar-ticket-registry-subscription`): Prefix of the subscription each CAS node uses to receive ticket registry messages. Every node must see every change, so a node subscribes under this prefix followed by its queue identifier (`cas.ticket.registry.core.queue-identifier`), giving each node a subscription of its own. Subscriptions are durable: give every node a stable queue identifier, otherwise each restart leaves an abandoned subscription behind that the broker keeps retaining messages for.
- `spring.pulsar.admin.authentication.param` (Third party; type `Map<String,String>`): Authentication parameter(s) as a map of parameter names to parameter values.
- `spring.pulsar.admin.authentication.plugin-class-name` (Third party; type `String`): Fully qualified class name of the authentication plugin.
- `spring.pulsar.admin.connection-timeout` (Third party; type `Duration`; default `1m`): Duration to wait for a connection to server to be established.
- `spring.pulsar.admin.read-timeout` (Third party; type `Duration`; default `1m`): Server response read time out for any request.
- `spring.pulsar.admin.request-timeout` (Third party; type `Duration`; default `5m`): Server request time out for any request.
- `spring.pulsar.admin.service-url` (Third party; type `String`; default `http://localhost:8080`): Pulsar web URL for the admin endpoint in the format '(http|https)://host:port'.
- `spring.pulsar.client.authentication.param` (Third party; type `Map<String,String>`): Authentication parameter(s) as a map of parameter names to parameter values.
- `spring.pulsar.client.authentication.plugin-class-name` (Third party; type `String`): Fully qualified class name of the authentication plugin.
- `spring.pulsar.client.connection-timeout` (Third party; type `Duration`; default `10s`): Duration to wait for a connection to a broker to be established.
- `spring.pulsar.client.failover.backup-clusters` (Third party; type `List<PulsarProperties.Failover.BackupCluster>`): List of backup clusters. The backup cluster is chosen in the sequence of the given list. If all backup clusters are available, the Pulsar client chooses the first backup cluster.
- `spring.pulsar.client.failover.check-interval` (Third party; type `Duration`): Frequency of performing a probe task.
- `spring.pulsar.client.failover.delay` (Third party; type `Duration`): Delay before the Pulsar client switches from the primary cluster to the backup cluster.
- `spring.pulsar.client.failover.policy` (Third party; type `AutoClusterFailoverBuilder.FailoverPolicy`; default `order`): Cluster failover policy.
- `spring.pulsar.client.failover.switch-back-delay` (Third party; type `Duration`): Delay before the Pulsar client switches from the backup cluster to the primary cluster.
- `spring.pulsar.client.lookup-timeout` (Third party; type `Duration`): Client lookup timeout.
- `spring.pulsar.client.operation-timeout` (Third party; type `Duration`; default `30s`): Client operation timeout.
- `spring.pulsar.client.service-url` (Third party; type `String`; default `pulsar://localhost:6650`): Pulsar service URL in the format '(pulsar|pulsar+ssl)://host:port'.
- `spring.pulsar.client.threads.io` (Third party; type `Integer`): Number of threads to be used for handling connections to brokers.
- `spring.pulsar.client.threads.listener` (Third party; type `Integer`): Number of threads to be used for message listeners.
- `spring.pulsar.consumer.dead-letter-policy.dead-letter-topic` (Third party; type `String`): Name of the dead topic where the failing messages will be sent.
- `spring.pulsar.consumer.dead-letter-policy.initial-subscription-name` (Third party; type `String`): Name of the initial subscription of the dead letter topic. When not set, the initial subscription will not be created. However, when the property is set then the broker's 'allowAutoSubscriptionCreation' must be enabled or the DLQ producer will fail.
- `spring.pulsar.consumer.dead-letter-policy.max-redeliver-count` (Third party; type `Integer`; default `0`): Maximum number of times that a message will be redelivered before being sent to the dead letter queue.
- `spring.pulsar.consumer.dead-letter-policy.retry-letter-topic` (Third party; type `String`): Name of the retry topic where the failing messages will be sent.
- `spring.pulsar.consumer.name` (Third party; type `String`): Consumer name to identify a particular consumer from the topic stats.
- `spring.pulsar.consumer.priority-level` (Third party; type `Integer`; default `0`): Priority level for shared subscription consumers.
- `spring.pulsar.consumer.read-compacted` (Third party; type `Boolean`; default `false`): Whether to read messages from the compacted topic rather than the full message backlog.
- `spring.pulsar.consumer.retry-enable` (Third party; type `Boolean`; default `false`): Whether to auto retry messages.
- `spring.pulsar.consumer.subscription.initial-position` (Third party; type `SubscriptionInitialPosition`; default `latest`): Position where to initialize a newly created subscription.
- `spring.pulsar.consumer.subscription.mode` (Third party; type `SubscriptionMode`; default `durable`): Subscription mode to be used when subscribing to the topic.
- `spring.pulsar.consumer.subscription.name` (Third party; type `String`): Subscription name for the consumer.
- `spring.pulsar.consumer.subscription.topics-mode` (Third party; type `RegexSubscriptionMode`; default `persistentonly`): Determines which type of topics (persistent, non-persistent, or all) the consumer should be subscribed to when using pattern subscriptions.
- `spring.pulsar.consumer.subscription.type` (Third party; type `SubscriptionType`; default `exclusive`): Subscription type to be used when subscribing to a topic.
- `spring.pulsar.consumer.topics` (Third party; type `List<String>`): Topics the consumer subscribes to.
- `spring.pulsar.consumer.topics-pattern` (Third party; type `Pattern`): Pattern for topics the consumer subscribes to.
- `spring.pulsar.defaults.topic.enabled` (Third party; type `Boolean`; default `true`): Whether to enable default tenant and namespace support for topics.
- `spring.pulsar.defaults.topic.namespace` (Third party; type `String`; default `default`): Default namespace to use when producing or consuming messages against a non-fully-qualified topic URL.
- `spring.pulsar.defaults.topic.tenant` (Third party; type `String`; default `public`): Default tenant to use when producing or consuming messages against a non-fully-qualified topic URL.
- `spring.pulsar.defaults.type-mappings` (Third party; type `List<PulsarProperties.Defaults.TypeMapping>`): List of mappings from message type to topic name and schema info to use as a defaults when a topic name and/or schema is not explicitly specified when producing or consuming messages of the mapped type.
- `spring.pulsar.function.enabled` (Third party; type `Boolean`; default `true`): Whether to enable function support.
- `spring.pulsar.function.fail-fast` (Third party; type `Boolean`; default `true`): Whether to stop processing further function creates/updates when a failure occurs.
- `spring.pulsar.function.propagate-failures` (Third party; type `Boolean`; default `true`): Whether to throw an exception if any failure is encountered during server startup while creating/updating functions.
- `spring.pulsar.function.propagate-stop-failures` (Third party; type `Boolean`; default `false`): Whether to throw an exception if any failure is encountered during server shutdown while enforcing stop policy on functions.
- `spring.pulsar.listener.concurrency` (Third party; type `Integer`): Number of threads used by listener container.
- `spring.pulsar.listener.observation-enabled` (Third party; type `Boolean`; default `false`): Whether to record observations for when the Observations API is available and the client supports it.
- `spring.pulsar.listener.schema-type` (Third party; type `SchemaType`): SchemaType of the consumed messages.
- `spring.pulsar.producer.access-mode` (Third party; type `ProducerAccessMode`; default `shared`): Type of access to the topic the producer requires.
- `spring.pulsar.producer.batching-enabled` (Third party; type `Boolean`; default `true`): Whether to automatically batch messages.
- `spring.pulsar.producer.cache.enabled` (Third party; type `Boolean`; default `true`): Whether to enable caching in the PulsarProducerFactory.
- `spring.pulsar.producer.cache.expire-after-access` (Third party; type `Duration`; default `1m`): Time period to expire unused entries in the cache.
- `spring.pulsar.producer.cache.initial-capacity` (Third party; type `Integer`; default `50`): Initial size of cache.
- `spring.pulsar.producer.cache.maximum-size` (Third party; type `Long`; default `1000`): Maximum size of cache (entries).
- `spring.pulsar.producer.chunking-enabled` (Third party; type `Boolean`; default `false`): Whether to split large-size messages into multiple chunks.
- `spring.pulsar.producer.compression-type` (Third party; type `CompressionType`): Message compression type.
- `spring.pulsar.producer.hashing-scheme` (Third party; type `HashingScheme`; default `javastringhash`): Message hashing scheme to choose the partition to which the message is published.
- `spring.pulsar.producer.message-routing-mode` (Third party; type `MessageRoutingMode`; default `roundrobinpartition`): Message routing mode for a partitioned producer.
- `spring.pulsar.producer.name` (Third party; type `String`): Name for the producer. If not assigned, a unique name is generated.
- `spring.pulsar.producer.send-timeout` (Third party; type `Duration`; default `30s`): Time before a message has to be acknowledged by the broker.
- `spring.pulsar.producer.topic-name` (Third party; type `String`): Topic the producer will publish to.
- `spring.pulsar.reader.name` (Third party; type `String`): Reader name.
- `spring.pulsar.reader.read-compacted` (Third party; type `Boolean`; default `false`): Whether to read messages from a compacted topic rather than a full message backlog of a topic.
- `spring.pulsar.reader.subscription-name` (Third party; type `String`): Subscription name.
- `spring.pulsar.reader.subscription-role-prefix` (Third party; type `String`): Prefix of subscription role.
- `spring.pulsar.reader.topics` (Third party; type `List<String>`): Topics the reader subscribes to.
- `spring.pulsar.template.observations-enabled` (Third party; type `Boolean`; default `false`): Whether to record observations for when the Observations API is available.
- `spring.pulsar.transaction.enabled` (Third party; type `Boolean`; default `false`): Whether transaction support is enabled.

**Signing & encryption**

This CAS feature is able to accept signing and encryption crypto keys. In most scenarios if keys are not provided, CAS will auto-generate them. The following instructions apply if you wish to manually and beforehand create the signing and encryption keys.

Note that if you are asked to create a [JWK](https://tools.ietf.org/html/rfc7517) of a certain size for the key, you are to use the following set of commands to generate the token:

```bash
wget https://raw.githubusercontent.com/apereo/cas/master/etc/jwk-gen.jar
java -jar jwk-gen.jar -t oct -s [size]
```

The outcome would be similar to:

```json
{
  "kty": "oct",
  "kid": "...",
  "k": "..."
}
```

The generated value for `k` needs to be assigned to the relevant CAS settings. Note that keys generated via the above algorithm are processed by CAS using the Advanced Encryption Standard (`AES`) algorithm which is a specification for the encryption of electronic data established by the U.S. National Institute of Standards and Technology.

---

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.

## Troubleshooting

To enable additional logging, configure the log4j configuration file to add the following levels:

```xml
...
<Logger name="org.springframework.pulsar" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>
<Logger name="org.apache.pulsar" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>
...
```
