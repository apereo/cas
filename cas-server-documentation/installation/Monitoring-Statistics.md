---
layout: default
title: CAS - Monitoring & Statistics
---

# Monitoring / Statistics
The CAS server exposes a `/status` endpoint that may be used to inquire about the health and general state of the software.

The following endpoints are secured and available:

| Parameter                         | Description
|-----------------------------------+-----------------------------------------+
| `/status`                         | Monitor information, see below.
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
| `/status/bus/refresh`             | Reload each application node’s configuration if the cloud bus is turned on.
| `/status/bus/env`                 | Sends key/values pairs to update each node's Spring Environment if the cloud bus is turned on.

## Security
Access is granted the following settings in `application.properties` file:

```properties
# cas.securityContext.adminpages.ip=127\.0\.0\.1
```

The `/status` endpoint is always protected by an IP pattern. The other administrative endpoints however can optionally 
be protected by the CAS server, via the following settings:

```properties
# cas.securityContext.adminpages.users=classpath:user-details.properties
# cas.securityContext.adminpages.adminRoles=ROLE_ADMIN
# cas.securityContext.adminpages.loginUrl=${server.prefix}/login
# cas.securityContext.adminpages.service=${server.prefix}/callback
```

The format of the `user-details.properties` file is as such:

```properties
# username=password,grantedAuthority
casuser=notused,ROLE_ADMIN
```

Failing to secure these endpoints via a CAS instance will have CAS fallback onto the IP range.

## Monitors

```bash
Health: OK

    1. MemoryMonitor: OK - 322.13MB free, 495.09MB total.
```

The list of configured monitors are all defined as:

```xml
<util:list id="monitorsList">
    <ref bean="memoryMonitor" />
    <ref bean="sessionMonitor" />
</util:list>
```

The following optional monitors are also available:

### Memcached

```xml
<dependency>
    <groupId>org.apereo.cas</groupId>
    <artifactId>cas-server-support-memcached-monitor</artifactId>
    <version>${cas.version}</version>
</dependency>
...

<util:list id="monitorsList">
    <ref bean="memcachedMonitor" />
</util:list>

...

```


The following settings are available:

```properties
# cache.monitor.warn.free.threshold=10
# cache.monitor.eviction.threshold=0
```

### Ehcache

```xml
<dependency>
    <groupId>org.apereo.cas</groupId>
    <artifactId>cas-server-support-ehcache-monitor</artifactId>
    <version>${cas.version}</version>
</dependency>

...

<util:list id="monitorsList">
    <ref bean="ehcacheMonitor" />
</util:list>
```

The following settings are available:

```properties
# cache.monitor.warn.free.threshold=10
# cache.monitor.eviction.threshold=0
```


### Hazelcast

```xml
<dependency>
    <groupId>org.apereo.cas</groupId>
    <artifactId>cas-server-support-hazelcast-monitor</artifactId>
    <version>${cas.version}</version>
</dependency>

...

<util:list id="monitorsList">
    <ref bean="hazelcastMonitor" />
</util:list>
```

The following settings are available:

```properties
# cache.monitor.warn.free.threshold=10
# cache.monitor.eviction.threshold=0
```


### JDBC

```xml

<dependency>
    <groupId>org.apereo.cas</groupId>
    <artifactId>cas-server-support-jdbc-monitor</artifactId>
    <version>${cas.version}</version>
</dependency>

...
<bean id="pooledConnectionFactoryMonitorExecutorService"
    class="org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean"
    p:corePoolSize="1"
    p:maxPoolSize="1"
    p:keepAliveSeconds="1" />

<util:list id="monitorsList">
    <ref bean="dataSourceMonitor" />
</util:list>

<alias name="myDataSource" alias="monitorDataSource" />

```

### LDAP

```xml

<dependency>
    <groupId>org.apereo.cas</groupId>
    <artifactId>cas-server-support-ldap-monitor</artifactId>
    <version>${cas.version}</version>
</dependency>

...

<util:list id="monitorsList">
    <ref bean="pooledLdapConnectionFactoryMonitor" />
</util:list>

<ldaptive:pooled-connection-factory
        id="pooledConnectionFactoryMonitorConnectionFactory"
        ldapUrl="${ldap.url}"
        blockWaitTime="${ldap.pool.blockWaitTime}"
        failFastInitialize="true"
        connectTimeout="${ldap.connectTimeout}"
        useStartTLS="${ldap.useStartTLS}"
        validateOnCheckOut="${ldap.pool.validateOnCheckout}"
        validatePeriodically="${ldap.pool.validatePeriodically}"
        validatePeriod="${ldap.pool.validatePeriod}"
        idleTime="${ldap.pool.idleTime}"
        maxPoolSize="${ldap.pool.maxSize}"
        minPoolSize="${ldap.pool.minSize}"
        useSSL="${ldap.use.ssl:false}"
        prunePeriod="${ldap.pool.prunePeriod}"
        provider="org.ldaptive.provider.unboundid.UnboundIDProvider"
/>

<bean id="pooledConnectionFactoryMonitorValidator" class="org.ldaptive.pool.SearchValidator" />

```

### Metric Refresh Interval
The metrics reporting interval can be configured via the `application.properties` file:

```properties
# Define how often should metric data be reported. Default is 30 seconds.
# metrics.refresh.interval=30
```

Various metrics can also be reported via JMX. Metrics are exposes via JMX MBeans.
Supported metrics include:

- Run count and elapsed times for all supported garbage collectors
- Memory usage for all memory pools, including off-heap memory
- Breakdown of thread states, including deadlocks
- File descriptor usage
- ...

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

type=METER, name=org.apereo.cas.CentralAuthenticationServiceImpl.CREATE_TICKET_GRANTING_TICKET_METER, count=0, mean_rate=0.0, m1=0.0, m5=0.0, m15=0.0, rate_unit=events/millisecond

type=METER, name=org.apereo.cas.CentralAuthenticationServiceImpl.DESTROY_TICKET_GRANTING_TICKET_METER, count=0, mean_rate=0.0, m1=0.0, m5=0.0, m15=0.0, rate_unit=events/millisecond

type=TIMER, name=org.apereo.cas.CentralAuthenticationServiceImpl.GRANT_SERVICE_TICKET_TIMER, count=0, min=0.0, max=0.0, mean=0.0, stddev=0.0, median=0.0, p75=0.0, p95=0.0, p98=0.0, p99=0.0, p999=0.0, mean_rate=0.0, m1=0.0, m5=0.0, m15=0.0, rate_unit=events/millisecond, duration_unit=milliseconds
```
