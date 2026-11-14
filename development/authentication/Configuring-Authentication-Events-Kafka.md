# Apache Kafka Authentication Events

Source: https://apereo.github.io/cas/development/authentication/Configuring-Authentication-Events-Kafka.html

Publishes authentication events into a Apache Kafka topic. This module only operates in a write-only mode and does not consume or fetch any events from Apache Kafka topics.

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-events-kafka"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.events.kafka.bootstrap-address` (Optional; type `String`): Kafka bootstrapping server address (i.e. localhost:9092).
- `cas.events.kafka.topic.compression-type` (Optional; type `String`; default `gzip`): Specify the final compression type for a given topic. This configuration accepts the standard compression codecs (`'gzip', 'snappy', 'lz4', 'zstd'`). It additionally accepts 'uncompressed' which is equivalent to no compression; and 'producer' which means retain the original compression codec set by the producer.
- `cas.events.kafka.topic.config` (Optional; type `Map<String,String>`): Additional configuration options, as pointed out by `TopicConfig`.
- `cas.events.kafka.topic.name` (Optional; type `String`): Set the name of the topic.
- `cas.events.kafka.topic.partitions` (Optional; type `Integer`; default `1`): Set the number of partitions (default 1).
- `cas.events.kafka.topic.replicas` (Optional; type `Integer`; default `1`): Set the number of replicas (default 1).
