---
layout: default
title: CAS - Monitoring & Statistics
category: Monitoring & Statistics
---

{% include variables.html %}

# Monitoring / Statistics

Actuator endpoints used to monitor and diagnose the internal configuration of the CAS server are typically
exposed over the endpoint `/actuator`.

## Actuator Endpoints

The following endpoints are provided:

{% include_cached actuators.html endpoints="info,startup,threaddump,health,metrics,httptrace,mappings,scheduledtasks,heapdump,prometheus,quartz" %}

## Metrics

Metrics allow to gain insight into the running CAS software, and provide 
ways to measure the behavior of critical components. 
See [this guide](Configuring-Metrics.html) for more info.

## Distributed Tracing

Support for distributed tracing of requests is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-sleuth" %}

![image](https://cloud.githubusercontent.com/assets/1205228/24955152/8798ad9c-1f97-11e7-8b9d-fccc3c306c42.png)

For most users [Sleuth](https://cloud.spring.io/spring-cloud-sleuth/) should be invisible, and all
interactions with external systems should be instrumented automatically.

Trace data is captured automatically and passed along to [Zipkin](https://github.com/openzipkin/zipkin), which helps 
gather timing data needed to troubleshoot latency problems.

{% include_cached casproperties.html thirdParty="spring.sleuth,spring.zipkin" %}

### Troubleshooting

To enable additional logging, modify the logging configuration file to add the following:

```xml
 <Logger name="org.springframework.cloud" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>
```

