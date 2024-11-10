---
layout: default
title: CAS - Configuration Management Clustered Deployment
category: Configuration
---

{% include variables.html %}

# Configuration Management - Clustered Deployments

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
of CAS settings in a shared (git) repository (or better yet, inside a private GitHub repository perhaps)
where you make a change in one place and it's broadcasted to all nodes. This model removes the need for
synchronizing changes across disks and CAS nodes.

{% include_cached casproperties.html thirdPartyStartsWith="spring.cloud.bus" %}
 
## Strategies
  
The following strategies are available to link the CAS nodes of a distributed deployment with a lightweight message broker,
to broadcast state changes (such as configuration changes) or other management instructions.

| Strategy     | Resource                                                         |
|--------------|------------------------------------------------------------------|
| AMQP         | See [this guide](Configuration-Management-Clustered-AMQP.html).  |
| Apache Kafka | See [this guide](Configuration-Management-Clustered-Kafka.html). |

## Actuator Endpoints

The following endpoints are provided by Spring Cloud:

{% include_cached actuators.html endpoints="features,refresh,busenv,bus-refresh,busrefresh,busshutdown,serviceregistry" %}

The transport mechanism for the bus to broadcast events is handled via one of the following components.

## Troubleshooting

To enable additional logging, modify the logging configuration file to add the following:

```xml
<Logger name="org.springframework.cloud.bus" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>
```
