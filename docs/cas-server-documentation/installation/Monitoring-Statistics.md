---
layout: default
title: CAS - Monitoring & Statistics
---

# Monitoring / Statistics

Actuator endpoints used to monitor and diagnose the internal configuration of the CAS server are typically
exposed over the endpoint `/actuator`. The following endpoints are secured and available by 
[Spring Boot actuators](http://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-endpoints.html):

| URL                       | Description
|---------------------------|-------------------------------------------------------------------------------------
| `autoconfig`              | Describes how the CAS application context is auto-configured. 
| `beans`                   | Displays all CAS application context **internal** Spring beans.
| `conditions`              | Shows the conditions that were evaluated on configuration and auto-configuration classes and the reasons why they did or did not match.
| `configprops`             | List of **internal** configuration properties.
| `threaddump`              | Produces a thread dump for the running CAS server.
| `env`                     | Produces a collection of all application properties.
| `health`                  | Reports back general health status of the system, produced by various monitors.
| `info`                    | CAS version information and other system traits.
| `metrics`                 | Runtime metrics and stats.
| `httptrace`               | Displays HTTP trace information (by default, the last 100 HTTP request-response exchanges).
| `mappings`                | Describes how requests are mapped and handled by CAS.
| `scheduledtasks`          | Displays the scheduled tasks in CAS.
| `mappings`                | Describes how requests are mapped and handled by CAS.
| `mappings`                | Describes how requests are mapped and handled by CAS.
| `shutdown`                | Shut down the application via a `POST`. Disabled by default.
| `restart`                 | Restart the application via a `POST`. Disabled by default.
| `refresh`                 | Refresh the application configuration via a `POST` to let components reload and recognize new values.
| `heapdump`                | Returns a GZip compressed hprof heap dump file.
| `jolokia`                 | Exposes JMX beans over HTTP when Jolokia is configured and included in CAS.
| `logfile`                 | Returns the contents of the log file if `logging.file` or `logging.path` properties are set with support for HTTP `Range` header.
| `prometheus`              | Exposes metrics in a format that can be scraped by a Prometheus server.
| `spring-webflow`          | Provides a JSON representation of the CAS authentication webflows.
| `events`                  | Provides a JSON representation of all CAS recorded events.
| `discovery-profile`       | Provides a JSON representation of the [CAS configuration and capabilities](Configuration-Discovery.html).
| `registered-services`     | Provides a JSON representation of the [CAS service registry](Service-Management.html).
| `configuration-metadata`  | Exposes [CAS configuration metadata](Configuration-Metadata-Repository.html) that can be used to query settings.
| `statistics`              | Exposes statistics data on tickets, memory, server availability and uptime, etc.
| `sso-sessions`            | Review the current single signon sessions establishes with CAS and manage each session remotely.
| `resolve-attributes/{name}`    | Invoke the CAS [attribute resolution](../integration/Attribute-Resolution.html) engine to locate attributes for `{name}`.
| `release-attributes`           | Invoke the CAS [attribute release](../integration/Attribute-Release.html) engine to release attributes to an application.
| `multifactor-trusted-devices`  | Expose devices currently [registered and trusted](Multifactor-TrustedDevice-Authentication.html) by the CAS multifactor authentication engine.
| `attribute-consent`  | Manage and control [attribute consent decisions](../integration/Attribute-Release-Consent.html).

<div class="alert alert-info"><strong>Exposed Endpoints</strong><p>
Note that by default the only endpoints exposed over the web are <code>info</code>, <code>health</code> and <code>configuration-metadata</code>.
Other endpoints need to be explicitly enabled and then exposed over the web in CAS settings in order to allow access.
</p></div>

Actuator endpoints provided by Spring Boot can also be visually managed and monitored
 via the [Spring Boot Administration Server](Configuring-Monitoring-Administration.html).
 
### Attribute Release Endpoint

Supported parameters are the following:

| Query Parameter           | Description
|---------------------------|--------------------------------------------
| `username`                | The username to use for authentication.
| `password`                | The password to use for authentication.
| `service`                | Service to which attributes should be released.

### Single SignOn Sessions Endpoint

A `GET` operation produces a list of current SSO sessions. A `DELETE` operation without 
specifying a ticket id will attempt to destroy all SSO sessions. Specifying a ticket-granting ticket identifier 
in the URL as a placeholder/selector will attempt to destroy the session controlled by that ticket. (i.e. `sso-sessions/{ticket}`)

### Multifactor Trusted Devices

A `GET` operation produces a list of all trusted devices. A `DELETE` operation with a 
a decision id will attempt to remove the consent decision (i.e. `attribute-consent/{/{id}`).
Specifying the `principal`  in the same manner will revoke all consent decisions for the user.

### Attribute Release Consent

A `GET` operation produces a list of all consent decisions. A `DELETE` operation with a 
a record key id will attempt to remove and revoke the registered device (i.e. `multifactor-trusted-devices/{key}`).


## Security

TODO:

## Health Monitors

Monitors allow you to watch the internal state of a given CAS component.
See [this guide](Configuring-Monitoring.html) for more info.

## Distributed Tracing

Support for distributed tracing of requests is enabled by including the following dependency in the WAR overlay:

```xml
<dependency>
     <groupId>org.apereo.cas</groupId>
     <artifactId>cas-server-support-sleuth</artifactId>
     <version>${cas.version}</version>
</dependency>
```

![image](https://cloud.githubusercontent.com/assets/1205228/24955152/8798ad9c-1f97-11e7-8b9d-fccc3c306c42.png)

For most users [Sleuth](https://cloud.spring.io/spring-cloud-sleuth/) should be invisible, and all
interactions with external systems should be instrumented automatically.

Trace data is captured automatically and passed along to [Zipkin](https://github.com/openzipkin/zipkin), which helps 
gather timing data needed to troubleshoot latency problems.

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#sleuth-distributed-tracing).

### Troubleshooting

To enable additional logging, modify the logging configuration file to add the following:

```xml
 <AsyncLogger name="org.springframework.cloud" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</AsyncLogger>
```

## Metrics

Metrics allow to gain insight into the running CAS software, and provide ways to measure the behavior of critical components. 
See [this guide](Configuring-Metrics.html) for more info.
