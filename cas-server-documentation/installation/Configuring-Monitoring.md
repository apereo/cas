---
layout: default
title: CAS - Monitoring
---

# CAS Monitoring

CAS monitors may be defined to report back the health status of the ticket registry
and other underlying connections to systems that are in use by CAS.

The list of configured monitors are all defined as:

```xml
<util:list id="monitorsList">
    <ref bean="memoryMonitor" />
    <ref bean="sessionMonitor" />
</util:list>
```

## Default

The default monitors report back brief memory and ticket stats. 

```bash
Health: OK

    1. MemoryMonitor: OK - 322.13MB free, 495.09MB total.
```

## Memcached

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


To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html).

## Ehcache

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

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html).


## Hazelcast

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

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html).


## JDBC

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

## LDAP

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
