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

See [this guide](Distributed-Tracing-Sleuth.html) for more info.