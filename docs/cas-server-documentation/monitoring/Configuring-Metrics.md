---
layout: default
title: CAS - Metrics
category: Monitoring & Statistics
---

{% include variables.html %}

# CAS Metrics

CAS via Spring Boot registers the following core metrics when applicable:

- Various memory and buffer pools
- Statistics related to garbage collection
- Threads utilization
- Number of classes loaded/unloaded
- CPU metrics
- File descriptor metrics
- Logback metrics: record the number of events logged to Logback at each level
- Uptime metrics: report a gauge for uptime and a fixed gauge representing the application’s absolute start time
- Apache Tomcat metrics
- Spring Integration metrics

Support is enabled by including the following module in the WAR Overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-metrics" %}

Auto-configuration enables the instrumentation of all available DataSource objects 
with a metric named `jdbc`. Data source instrumentation results in gauges representing the 
currently active, maximum allowed, and minimum allowed connections in the pool. 
Each of these gauges has a name that is prefixed by `jdbc`. Metrics are also tagged by the name of the `DataSource` 
computed based on the bean name. Also, Hikari-specific metrics are exposed 
with a `hikaricp` prefix. Each metric is tagged by the name of the Pool.

Auto-configuration enables the instrumentation of all available caches on startup with metrics prefixed with cache. 
Cache instrumentation is standardized for a basic set of metrics. Additional, cache-specific metrics are also available.

CAS Metrics are accessed and queried using the CAS actuator admin endpoints. 
Navigating to the endpoint displays a list of available meter names. 
You can drill down to view information about a particular meter by providing its name as a selector.

[See this guide](Monitoring-Statistics.html) to learn more.

## Actuator Endpoints

The following endpoints are provided by CAS:

{% include actuators.html endpoints="statistics" casModule="cas-server-support-reports" %}

## Metrics Customization

Please [see this guide](Configuring-Metrics-Custom.html).         

## Storage

Please [see this guide](Configuring-Metrics-Storage.html).
