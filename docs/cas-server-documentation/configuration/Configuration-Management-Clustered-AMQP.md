---
layout: default
title: CAS - Configuration Management Clustered Deployment
category: Configuration
---

{% include variables.html %}

# Configuration Management - Clustered Deployments with AMQP

This is one option for broadcasting change events to CAS nodes.
[RabbitMQ](https://www.rabbitmq.com/) is open source message broker
software (sometimes called message-oriented middleware) that implements
the Advanced Message Queuing Protocol (AMQP).

Support is enabled by including the following dependency in the final overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-configuration-cloud-amqp" %}

{% include_cached casproperties.html thirdPartyStartsWith="spring.rabbitmq." %}

## Troubleshooting

To enable additional logging, modify the logging configuration file to add the following:

```xml
<Logger name="org.springframework.amqp" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>
```
