# Configuration Management - Clustered Deployments with Kafka

Source: https://apereo.github.io/cas/development/configuration/Configuration-Management-Clustered-Kafka.html

Apache Kafka is an open-source message broker project developed by the Apache Software Foundation. The project aims to provide a unified, high-throughput, low-latency platform for handling real-time data feeds. It is, in its essence, a “massively scalable pub/sub message queue designed as a distributed transaction log”, making it highly valuable for enterprise infrastructures to process streaming data.

Support is enabled by including the following dependency in the final overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-configuration-cloud-kafka"
}
```

Broadcast CAS configuration updates to other nodes in the cluster via [Kafka](http://docs.spring.io/spring-cloud-stream/docs/current/reference/htmlsingle/#_apache_kafka_binder).

The following settings and properties are available from the CAS configuration catalog:

- `spring.cloud.stream.kafka.binder.authorization-exception-retry-interval` (Third party; type `Duration`)
- `spring.cloud.stream.kafka.binder.auto-add-partitions` (Third party; type `Boolean`)
- `spring.cloud.stream.kafka.binder.auto-alter-topics` (Third party; type `Boolean`)
- `spring.cloud.stream.kafka.binder.auto-create-topics` (Third party; type `Boolean`)
- `spring.cloud.stream.kafka.binder.brokers` (Third party; type `String[]`)
- `spring.cloud.stream.kafka.binder.certificate-store-directory` (Third party; type `String`)
- `spring.cloud.stream.kafka.binder.configuration` (Third party; type `Map<String,String>`)
- `spring.cloud.stream.kafka.binder.consider-down-when-any-partition-has-no-leader` (Third party; type `Boolean`)
- `spring.cloud.stream.kafka.binder.consumer-properties` (Third party; type `Map<String,String>`)
- `spring.cloud.stream.kafka.binder.enable-observation` (Third party; type `Boolean`)
- `spring.cloud.stream.kafka.binder.header-mapper-bean-name` (Third party; type `String`)
- `spring.cloud.stream.kafka.binder.headers` (Third party; type `String[]`)
- `spring.cloud.stream.kafka.binder.health-indicator-consumer-group` (Third party; type `String`)
- `spring.cloud.stream.kafka.binder.health-timeout` (Third party; type `Integer`)
- `spring.cloud.stream.kafka.binder.jaas` (Third party; type `JaasLoginModuleConfiguration`)
- `spring.cloud.stream.kafka.binder.metrics.default-offset-lag-metrics-enabled` (Third party; type `Boolean`)
- `spring.cloud.stream.kafka.binder.metrics.offset-lag-metrics-interval` (Third party; type `Duration`)
- `spring.cloud.stream.kafka.binder.min-partition-count` (Third party; type `Integer`)
- `spring.cloud.stream.kafka.binder.producer-properties` (Third party; type `Map<String,String>`)
- `spring.cloud.stream.kafka.binder.replication-factor` (Third party; type `Short`)
- `spring.cloud.stream.kafka.binder.required-acks` (Third party; type `String`)
- `spring.cloud.stream.kafka.binder.transaction.max-age` (Third party; type `Duration`)
- `spring.cloud.stream.kafka.binder.transaction.producer.batch-timeout` (Third party; type `Integer`)
- `spring.cloud.stream.kafka.binder.transaction.producer.buffer-size` (Third party; type `Integer`)
- `spring.cloud.stream.kafka.binder.transaction.producer.compression-type` (Third party; type `KafkaProducerProperties.CompressionType`)
- `spring.cloud.stream.kafka.binder.transaction.producer.configuration` (Third party; type `Map<String,String>`)
- `spring.cloud.stream.kafka.binder.transaction.producer.error-channel-enabled` (Third party; type `Boolean`)
- `spring.cloud.stream.kafka.binder.transaction.producer.header-mode` (Third party; type `HeaderMode`)
- `spring.cloud.stream.kafka.binder.transaction.producer.header-patterns` (Third party; type `String[]`)
- `spring.cloud.stream.kafka.binder.transaction.producer.message-key-expression` (Third party; type `Expression`)
- `spring.cloud.stream.kafka.binder.transaction.producer.partition-count` (Third party; type `Integer`)
- `spring.cloud.stream.kafka.binder.transaction.producer.partition-key-expression` (Third party; type `Expression`)
- `spring.cloud.stream.kafka.binder.transaction.producer.partition-key-extractor-name` (Third party; type `String`)
- `spring.cloud.stream.kafka.binder.transaction.producer.partition-selector-expression` (Third party; type `Expression`)
- `spring.cloud.stream.kafka.binder.transaction.producer.partition-selector-name` (Third party; type `String`)
- `spring.cloud.stream.kafka.binder.transaction.producer.required-groups` (Third party; type `String[]`)
- `spring.cloud.stream.kafka.binder.transaction.producer.sync` (Third party; type `Boolean`)
- `spring.cloud.stream.kafka.binder.transaction.producer.topic` (Third party; type `KafkaTopicProperties`)
- `spring.cloud.stream.kafka.binder.transaction.producer.use-native-encoding` (Third party; type `Boolean`)
- `spring.cloud.stream.kafka.binder.transaction.transaction-id-prefix` (Third party; type `String`)
- `spring.cloud.stream.kafka.bindings` (Third party; type `Map<String,KafkaBindingProperties>`)

## Troubleshooting

To enable additional logging, modify the logging configuration file to add the following:

```xml
<Logger name="org.springframework.kafka" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>
```
