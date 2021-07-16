---
layout: default
title: CAS - Configuration Management Clustered Deployment
category: Configuration
---

{% include variables.html %}

# Clustered Deployments

CAS uses the [Spring Cloud Bus](http://cloud.spring.io/spring-cloud-static/spring-cloud.html)
to manage configuration in a distributed deployment. Spring Cloud Bus links nodes of a
distributed system with a lightweight message broker. This can then be used to broadcast state
changes (e.g. configuration changes) or other management instructions.

The bus supports sending messages to all nodes listening. Broadcasted events will attempt to update, refresh and
reload each CAS server application’s configuration.

If CAS nodes are not sharing a central location for configuration properties such that each
node contains a copy of the settings, any changes you make to one node must be replicated and
synced across all nodes so they are persisted on disk. The broadcast mechanism noted above only
applies changes to the runtime and the running CAS instance. Ideally, you should be keeping track
of CAS settings in a shared (git) repository (or better yet, inside a private Github repository perhaps)
where you make a change in one place and it's broadcasted to all nodes. This model removes the need for
synchronizing changes across disks and CAS nodes.CAS uses the Spring Cloud Bus to manage configuration 
in a distributed deployment. Spring Cloud Bus links nodes of a distributed system with a lightweight message broker.

{% include casproperties.html thirdPartyStartsWith="spring.cloud.bus" %}

The following endpoints are provided by Spring Cloud:

{% include actuators.html endpoints="features,refresh,busenv,bus-refresh,busrefresh,serviceregistry" %}

The transport mechanism for the bus to broadcast events is handled via one of the following components.

## Troubleshooting

To enable additional logging, modify the logging configuration file to add the following:

```xml
<Logger name="org.springframework.amqp" level="debug" additivity="false">
    <AppenderRef ref="console"/>
    <AppenderRef ref="file"/>
</Logger>
```

## RabbitMQ

This is the default option for broadcasting change events to CAS nodes.
[RabbitMQ](https://www.rabbitmq.com/) is open source message broker
software (sometimes called message-oriented middleware) that implements
the Advanced Message Queuing Protocol (AMQP).

Support is enabled by including the following dependency in the final overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-configuration-cloud-amqp" %}

{% include casproperties.html thirdPartyStartsWith="spring.rabbitmq." %}

## Kafka

Apache Kafka is an open-source message broker project developed by the Apache Software Foundation.
The project aims to provide a unified, high-throughput, low-latency platform for handling real-time data feeds.
It is, in its essence, a "massively scalable pub/sub message queue designed as a distributed transaction log",
making it highly valuable for enterprise infrastructures to process streaming data.

Support is enabled by including the following dependency in the final overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-configuration-cloud-kafka" %}

Broadcast CAS configuration updates to other nodes in the cluster
via [Kafka](http://docs.spring.io/spring-cloud-stream/docs/current/reference/htmlsingle/#_apache_kafka_binder).
  
{% include casproperties.html thirdPartyStartsWith="spring.cloud.stream.kafka,spring.cloud.stream.bindings.output" %}
