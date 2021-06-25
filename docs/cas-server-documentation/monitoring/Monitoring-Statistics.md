---
layout: default
title: CAS - Monitoring & Statistics
category: Monitoring & Statistics
---

{% include variables.html %}

# Monitoring / Statistics

Actuator endpoints used to monitor and diagnose the internal configuration of the CAS server are typically
exposed over the endpoint `/actuator`.

## Spring Boot Endpoints

The following endpoints are secured and available by 
[Spring Boot actuators](http://docs.spring.io/spring-boot/docs/current/reference/html/):

| Endpoint                  | Description
|---------------------------|-------------------------------------------------------------------------------------
| `beans`                   | Displays all CAS application context **internal** Spring beans.
| `conditions`              | Shows the conditions that were evaluated on configuration and auto-configuration classes and the reasons.
| `configprops`             | List of **internal** configuration properties.
| `startup`                 | Analyze application startup events, beans and load and optionally report events to Java Flight Recorder.
| `threaddump`              | Produces a thread dump for the running CAS server.
| `env`                     | Produces a collection of all application properties.
| `health`                  | Reports back general health status of the system, produced by various monitors.
| `info`                    | CAS version information and other system traits.
| `metrics`                 | Runtime metrics and stats.
| `loggers`                 | Logger configuration and levels.
| `httptrace`               | Displays HTTP trace information (by default, the last 100 HTTP request-response exchanges).
| `mappings`                | Describes how requests are mapped and handled by CAS.
| `scheduledtasks`          | Displays the scheduled tasks in CAS.
| `shutdown`                | Shut down the application via a `POST`. Disabled by default.
| `restart`                 | Restart the application via a `POST`. Disabled by default.
| `refresh`                 | Refresh the application configuration via a `POST` to let components reload and recognize new values.
| `heapdump`                | Returns a GZip compressed hprof heap dump file.
| `jolokia`                 | Exposes JMX beans over HTTP when Jolokia is configured and included in CAS.
| `logfile`                 | Returns the log file content if `logging.file` or `logging.path` are set with support for HTTP `Range` header.
| `prometheus`              | Exposes metrics in a format that can be scraped by a Prometheus server.


Actuator endpoints provided by Spring Boot can also be visually managed and monitored
via the [Spring Boot Administration Server](Configuring-Monitoring-Administration.html).

## Metrics

Metrics allow to gain insight into the running CAS software, and provide 
ways to measure the behavior of critical components. 
See [this guide](Configuring-Metrics.html) for more info.

### JAAS Authentication Security

{% include casproperties.html properties="cas.monitor.endpoints.jaas" %}

### LDAP Authentication Security

{% include casproperties.html properties="cas.monitor.endpoints.ldap" %}

### JDBC Authentication Security

{% include casproperties.html properties="cas.monitor.endpoints.jdbc" %}


### Endpoint Security

Note that any individual endpoint must be first enabled before any security
can be applied. The security of all endpoints is controlled using the following settings:

{% include casproperties.html 
properties="cas.monitor.endpoints.form-login-enabled,cas.monitor.endpoints.endpoint" %}

## Distributed Tracing

Support for distributed tracing of requests is enabled by including the following dependency in the WAR overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-sleuth" %}

![image](https://cloud.githubusercontent.com/assets/1205228/24955152/8798ad9c-1f97-11e7-8b9d-fccc3c306c42.png)

For most users [Sleuth](https://cloud.spring.io/spring-cloud-sleuth/) should be invisible, and all
interactions with external systems should be instrumented automatically.

Trace data is captured automatically and passed along to [Zipkin](https://github.com/openzipkin/zipkin), which helps 
gather timing data needed to troubleshoot latency problems.

{% include casproperties.html thirdParty="spring.sleuth,spring.zipkin" %}

### Troubleshooting

To enable additional logging, modify the logging configuration file to add the following:

```xml
 <Logger name="org.springframework.cloud" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>
```

