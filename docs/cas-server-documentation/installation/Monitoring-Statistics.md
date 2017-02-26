---
layout: default
title: CAS - Monitoring & Statistics
---

# Monitoring / Statistics

The following endpoints are secured and available by CAS:

| URL                               | Description
|-----------------------------------|------------------------------------------
| `/status/dashboard`               | A good starting point, that is a control panel to CAS server functionality and management. *
| `/status`                         | [Monitor CAS status and other underlying components](Configuring-Monitoring.html).
| `/status/sso`                     | Describes how the CAS application context is auto-configured.
| `/status/stats`                   | Visual representation of CAS statistics with graphs and charts, etc.
| `/status/logging`                 | Monitor CAS logs in a streaming fashion, and review the audit log.
| `/status/config`                  | Visual representation of **CAS** application properties and configuration.
| `/status/ssosessions`             | Report of active SSO sessions and authentications. Examine attributes, services and log users out.
| `/status/trustedDevs`             | Reports on the [registered trusted devices/browsers](Multifactor-TrustedDevice-Authentication.html).
| `/status/authnEvents`             | When enabled, report on the [events captured by CAS](Configuring-Authentication-Events.html).
| `/status/attrresolution`          | Examine resolution of user attributes via [CAS attribute resolution](../integration/Attribute-Resolution.html).

The following endpoints are secured and available by [Spring Boot actuators](http://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-endpoints.html):

| URL                               | Description
|-----------------------------------|-------------------------------------------------------------------------------------
| `/status/autoconfig`              | Describes if there exists an active SSO session for this session.
| `/status/beans`                   | Displays all CAS application context **internal** Spring beans.
| `/status/configprops`             | List of **internal** configuration properties.
| `/status/dump`                    | Produces a thread dump for the running CAS server.
| `/status/env`                     | Produces a collection of all application properties.
| `/status/health`                  | Reports back general health status of the system, produced by various monitors.
| `/status/info`                    | CAS version information and other system traits.
| `/status/metrics`                 | Runtime metrics and stats.
| `/status/stats`                   | Visual representation of CAS statistics with graphs and charts, etc.
| `/status/mappings`                | Describes how requests are mapped and handled by CAS.
| `/status/shutdown`                | Shut down the application via a `POST`. Disabled by default.
| `/status/restart`                 | Restart the application via a `POST`. Disabled by default.
| `/status/refresh`                 | Refresh the application configuration via a `POST` to let components reload and recognize new values.

## Security

All urls that are scoped to the `/status` endpoint are modeled after Spring Boot's own actuator endpoints
and by default are considered `sensitive`. By default, no endpoint is enabled or allowed access.

Endpoints may go through multiple levels and layers of security described here:

- All endpoints may be globally considered `sensitive`.
- Spring Boot's actuator endpoints may be individually marked as `sensitive` or `enabled`.
- Similarly, CAS endpoints may be individually marked as `sensitive` or `enabled`.
- In the event that access to an endpoint is allowed, (i.e endpoint is enabled and is not marked as sensitive), CAS will attempt
to control access by enforcing rules via IP address matching, delegating to itself, etc. The `/status` endpoint is always protected by an IP pattern. The other administrative endpoints however can optionally be protected by the CAS server. Failing to secure these endpoints via a CAS instance will have CAS fallback onto the IP range.
    - If you decide to protect other administrative endpoints via CAS itself, you will need to provide
    a reference to the list of authorized users in the CAS configuration. You may also enforce authorization
    rules via [Service-based Access Strategy](Configuring-Service-Access-Strategy.html) features of CAS.

<div class="alert alert-warning"><strong>Reverse Proxies</strong><p>Allowing access to the <code>/status</code> endpoint
via IP address matching needs to be very carefully designed, specially in cases where CAS is deployed behind a proxy
such as Apache. Be sure to test access rules and policies carefully or otherwise devise your own.</p></div>

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#admin-status-endpoints).

### Spring Security

Alternatively, you may design the security of CAS `/status` endpoints to take advantage
of [Spring Security](http://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-monitoring.html).
After all, that's what `sensitive` is designed to do. Using this model and via CAS settings, you get to define 
the authentication scheme (i.e. `BASIC`) as well
as the protected/ignored paths and pre-defined "master" username/password that is used for authentication.
If the password is left blank, a random password will be generated/printed in the logs by default.
Besides the master credentials, backend authentication support via LDAP and JDBC storage facilities are also available.

Support is enabled by including the following module in the WAR overlay:

```xml
<dependency>
  <groupId>org.apereo.cas</groupId>
  <artifactId>cas-server-webapp-config-security</artifactId>
  <version>${cas.version}</version>
</dependency>
```

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#admin-status-endpoints-with-spring-security).

## Monitors

Monitors allow you to watch the internal state of a given CAS component.
See [this guide](Configuring-Monitoring.html) for more info.

## Metrics

Supported metrics include:

- Run count and elapsed times for all supported garbage collectors
- Memory usage for all memory pools, including off-heap memory
- Breakdown of thread states, including deadlocks
- File descriptor usage
- ...

### Metric Refresh Interval

The metrics reporting interval can be configured via CAS properties.
To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#metrics--performance-stats).

### Loggers

All performance data and metrics are routed to a log file via the Log4j configuration:

```xml
...
<RollingFile name="perfFileAppender" fileName="perfStats.log" append="true"
             filePattern="perfStats-%d{yyyy-MM-dd-HH}-%i.log">
    <PatternLayout pattern="%m%n"/>
    <Policies>
        <OnStartupTriggeringPolicy />
        <SizeBasedTriggeringPolicy size="10 MB"/>
        <TimeBasedTriggeringPolicy />
    </Policies>
</RollingFile>

...

<CasAppender name="casPerf">
    <AppenderRef ref="perfFileAppender" />
</CasAppender>

```


### Sample Output

```bash
type=GAUGE, name=jvm.gc.Copy.count, value=22
type=GAUGE, name=jvm.gc.Copy.time, value=466
type=GAUGE, name=jvm.gc.MarkSweepCompact.count, value=3
type=GAUGE, name=jvm.gc.MarkSweepCompact.time, value=414
type=GAUGE, name=jvm.memory.heap.committed, value=259653632
type=GAUGE, name=jvm.memory.heap.init, value=268435456
type=GAUGE, name=jvm.memory.heap.max, value=1062338560
type=GAUGE, name=jvm.memory.heap.usage, value=0.09121857348376773
type=GAUGE, name=jvm.memory.heap.used, value=96905008

type=METER, name=org.apereo.cas.DefaultCentralAuthenticationService.CREATE_TICKET_GRANTING_TICKET_METER, count=0,
mean_rate=0.0, m1=0.0, m5=0.0, m15=0.0, rate_unit=events/millisecond

type=METER, name=org.apereo.cas.DefaultCentralAuthenticationService.DESTROY_TICKET_GRANTING_TICKET_METER,
count=0, mean_rate=0.0, m1=0.0, m5=0.0, m15=0.0, rate_unit=events/millisecond

type=TIMER, name=org.apereo.cas.DefaultCentralAuthenticationService.GRANT_SERVICE_TICKET_TIMER, count=0,
min=0.0, max=0.0, mean=0.0, stddev=0.0, median=0.0, p75=0.0, p95=0.0, p98=0.0, p99=0.0, p999=0.0,
mean_rate=0.0, m1=0.0, m5=0.0, m15=0.0, rate_unit=events/millisecond, duration_unit=milliseconds
```
