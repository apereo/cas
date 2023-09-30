---
layout: default
title: CAS - Configuration Management Clustered Deployment
category: Configuration
---

{% include variables.html %}

# Configuration Management - Clustered Deployments with Kafka

Apache Kafka is an open-source message broker project developed by the Apache Software Foundation.
The project aims to provide a unified, high-throughput, low-latency platform for handling real-time data feeds.
It is, in its essence, a "massively scalable pub/sub message queue designed as a distributed transaction log",
making it highly valuable for enterprise infrastructures to process streaming data.

Support is enabled by including the following dependency in the final overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-configuration-cloud-kafka" %}

Broadcast CAS configuration updates to other nodes in the cluster
via [Kafka](http://docs.spring.io/spring-cloud-stream/docs/current/reference/htmlsingle/#_apache_kafka_binder).
  
{% include_cached casproperties.html thirdPartyStartsWith="spring.cloud.stream.kafka,spring.cloud.stream.bindings.output" %}

## Troubleshooting

To enable additional logging, modify the logging configuration file to add the following:

```xml
<Logger name="org.springframework.kafka" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>
```
