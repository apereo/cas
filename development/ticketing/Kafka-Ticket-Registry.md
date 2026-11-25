# Apache Kafka Ticket Registry

Source: https://apereo.github.io/cas/development/ticketing/Kafka-Ticket-Registry.html

The Apache Kafka ticket registry is very much an extension of the [default ticket registry](https://apereo.github.io/cas/development/ticketing/Default-Ticket-Registry.html). The difference is that ticket operations applied to the registry are broadcasted using Kafka topics to other listening CAS nodes. Each node keeps copies of ticket state on its own and only instructs others to keep their copy accurate by broadcasting messages and data associated with each. Each message and ticket registry instance running inside a CAS node in the cluster is tagged with a unique identifier in order to avoid endless looping behavior and recursive needless inbound operations.

The broadcast and pub/sub mechanism is backed by [Apache Kafka](https://kafka.apache.org/). Apache Kafka is a distributed event streaming platform designed for handling real-time data feeds. Kafka is highly scalable and fault-tolerant, making it ideal for building real-time data pipelines and streaming applications.

Key features of Apache Kafka include:

- **Message Broker**: Kafka acts as a distributed messaging system that enables the publishing, storing, and processing of streams of data in real-time.
- **Producers and Consumers**: Data is produced by producers and consumed by consumers in Kafka. Producers send messages to Kafka topics, and consumers read messages from these topics.
- **Topics**: A topic is a category or stream name to which records are sent. Data is partitioned across multiple Kafka brokers based on topics.

Support is enabled by including the following dependency in the overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-kafka-ticket-registry"
}
```

## CAS Configuration

The following settings and properties are available from the CAS configuration catalog:

- `cas.ticket.registry.in-memory.crypto.encryption.key` (Required; type `String`): The encryption key. The encryption key by default and unless specified otherwise must be randomly-generated string whose length is defined by the encryption key size setting.
- `cas.ticket.registry.in-memory.crypto.signing.key` (Required; type `String`): The signing key is a string whose length is defined by the signing key size setting.
- `cas.ticket.registry.kafka.crypto.encryption.key` (Required; type `String`): The encryption key. The encryption key by default and unless specified otherwise must be randomly-generated string whose length is defined by the encryption key size setting.
- `cas.ticket.registry.kafka.crypto.signing.key` (Required; type `String`): The signing key is a string whose length is defined by the signing key size setting.
- `cas.ticket.registry.in-memory.concurrency` (Optional; type `Integer`; default `20`): The estimated number of concurrently updating threads. The implementation performs internal sizing to try to accommodate this many threads.
- `cas.ticket.registry.in-memory.crypto.alg` (Optional; type `String`; default `AES`): The signing/encryption algorithm to use.
- `cas.ticket.registry.in-memory.crypto.enabled` (Optional; type `Boolean`; default `true`): Whether crypto operations are enabled.
- `cas.ticket.registry.in-memory.crypto.encryption.key-size` (Optional; type `Integer`; default `16`): Encryption key size.
- `cas.ticket.registry.in-memory.crypto.signing-enabled` (Optional; type `Boolean`; default `true`): Whether signing encryption operations are enabled.
- `cas.ticket.registry.in-memory.crypto.signing.key-size` (Optional; type `Integer`; default `512`): The signing key size.
- `cas.ticket.registry.in-memory.initial-capacity` (Optional; type `Integer`; default `1000`): The initial capacity of the underlying memory store. The implementation performs internal sizing to accommodate this many elements.
- `cas.ticket.registry.in-memory.load-factor` (Optional; type `Integer`; default `1`): The load factor threshold, used to control resizing. Resizing may be performed when the average number of elements per bin exceeds this threshold.
- `cas.ticket.registry.in-memory.properties` (Optional; type `Map<String,String>`)
- `cas.ticket.registry.kafka.auto-create-topics` (Optional; type `Boolean`; default `true`): Whether the registry should auto-create topics.
- `cas.ticket.registry.kafka.bootstrap-address` (Optional; type `String`): Kafka bootstrapping server address (i.e. localhost:9092).
- `cas.ticket.registry.kafka.concurrency` (Optional; type `Integer`): The concurrency level in ConcurrentMessageListenerContainer determines the number of concurrent threads that will be used to process messages. This allows you to parallelize message consumption, which can improve throughput and performance. By default, this is the number of available processors (CPU cores) to the Java virtual machine.
- `cas.ticket.registry.kafka.crypto.alg` (Optional; type `String`; default `AES`): The signing/encryption algorithm to use.
- `cas.ticket.registry.kafka.crypto.enabled` (Optional; type `Boolean`; default `true`): Whether crypto operations are enabled.
- `cas.ticket.registry.kafka.crypto.encryption.key-size` (Optional; type `Integer`; default `16`): Encryption key size.
- `cas.ticket.registry.kafka.crypto.signing-enabled` (Optional; type `Boolean`; default `true`): Whether signing encryption operations are enabled.
- `cas.ticket.registry.kafka.crypto.signing.key-size` (Optional; type `Integer`; default `512`): The signing key size.
- `cas.ticket.registry.kafka.group-id` (Optional; type `String`; default `cas-ticket-registry`): Prefix of the consumer group each CAS node uses to consume ticket registry messages. Every node keeps its own copy of the registry and must receive every change, so the group is completed with the node's queue identifier (`cas.ticket.registry.core.queue-identifier`, or a random value when that is not set) and no two nodes share a group. Nodes that share a group split the topic partitions between them, so each change would reach only one of them. Setting the queue identifier keeps a node's group, and its committed offsets, stable across restarts.

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

## Troubleshooting

To enable additional logging, configure the log4j configuration file to add the following levels:

```xml
...
<Logger name="org.apache.kafka" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>
...
```
