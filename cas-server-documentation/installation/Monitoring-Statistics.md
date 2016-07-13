---
layout: default
title: CAS - Monitoring & Statistics
---

# Monitoring / Statistics

The CAS server exposes a `/status` endpoint that may be used to inquire about
 the health and general state of the software.

The following endpoints are secured and available:

| Parameter                         | Description
|-----------------------------------+-----------------------------------------+
| `/status`                         | [Monitor status information](Configuring-Monitoring.html).
| `/status/autoconfig`              | Describes how the application context is auto configured.
| `/status/beans`                   | Displays all application context beans.
| `/status/configprops`             | List of **internal** configuration properties.
| `/status/dump`                    | Produces a thread dump.
| `/status/env`                     | Produces a collection of application properties.
| `/status/health`                  | General health of the system.
| `/status/info`                    | CAS version information.
| `/status/metrics`                 | Runtime metrics and stats.
| `/status/stats`                   | Visual representation of CAS statistics.
| `/status/config`                  | Visual representation of application properties and configuration.
| `/status/mappings`                | Describes how requests are mapped and handled by CAS.
| `/status/shutdown`                | Shut down the application via a `POST`. Disabled by default.
| `/status/restart`                 | Restart the application via a `POST`. Disabled by default.
| `/status/refresh`                 | Refresh the application configuration via a `POST`.
| `/status/dashboard`               | Control panel to CAS server functionality and management.
| `/status/ssosessions`             | Report of active SSO sessions and authentications.
| `/status/bus/refresh`             | Reload CAS node’s configuration if the cloud bus is turned on.
| `/status/bus/env`        | Sends key/values pairs to update each CAS node if the cloud bus is turned on.

## Security

Access is granted the following settings in `application.properties` file.
The `/status` endpoint is always protected by an IP pattern. The other administrative 
endpoints however can optionally 
be protected by the CAS server.
Failing to secure these endpoints via a CAS instance will have CAS fallback onto the IP range.

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html).

## Monitors

See [this guide](Configuring-Monitoring.html) for more info. 

## Metrics

Supported metrics include:

- Run count and elapsed times for all supported garbage collectors
- Memory usage for all memory pools, including off-heap memory
- Breakdown of thread states, including deadlocks
- File descriptor usage
- ...

### Metric Refresh Interval

The metrics reporting interval can be configured via the `application.properties` file.
To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html).

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

type=METER, name=org.apereo.cas.CentralAuthenticationServiceImpl.CREATE_TICKET_GRANTING_TICKET_METER, count=0, 
mean_rate=0.0, m1=0.0, m5=0.0, m15=0.0, rate_unit=events/millisecond

type=METER, name=org.apereo.cas.CentralAuthenticationServiceImpl.DESTROY_TICKET_GRANTING_TICKET_METER, 
count=0, mean_rate=0.0, m1=0.0, m5=0.0, m15=0.0, rate_unit=events/millisecond

type=TIMER, name=org.apereo.cas.CentralAuthenticationServiceImpl.GRANT_SERVICE_TICKET_TIMER, count=0, 
min=0.0, max=0.0, mean=0.0, stddev=0.0, median=0.0, p75=0.0, p95=0.0, p98=0.0, p99=0.0, p999=0.0, 
mean_rate=0.0, m1=0.0, m5=0.0, m15=0.0, rate_unit=events/millisecond, duration_unit=milliseconds
```
