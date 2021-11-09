---
layout: default
title: CAS - Monitoring
category: Monitoring & Statistics
---

{% include variables.html %}

# CAS Monitoring

CAS monitors may be defined to report back the health status of the ticket registry 
and other underlying connections to systems that are in use by CAS. Spring Boot 
offers a number of monitors known as `HealthIndicator`s that are activated given 
the presence of specific settings (i.e. `spring.mail.*`). CAS itself providers a 
number of other monitors based on the same component that are listed below, whose 
action may require a combination of a particular dependency module and its relevant settings.

## Default

The default monitors report back brief memory and ticket stats.

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-core-monitor" %}

{% include_cached casproperties.html 
properties="cas.monitor.st,cas.monitor.tgt,cas.monitor.load,cas.monitor.memory" %}

<div class="alert alert-warning"><strong>YMMV</strong><p>In order to accurately and reliably 
report on ticket statistics, you are at the mercy of the underlying ticket registry to support 
the behavior in a performant manner which means that the infrastructure and network capabilities 
and latencies must be considered and carefully tuned. This might have become specially relevant 
in clustered deployments as depending on the ticket registry of choice, CAS may need 
to <i>interrogate</i> the entire cluster by running distributed queries to calculate ticket usage.</p></div>

### Advanced

Monitors can also be managed using any one of the following strategies.

| Storage        | Description                                         
|----------------------------------------------------------------------------
| Memcached      | [this guide](Configuring-Monitoring-Memcached.html)  
| Ehcache        | [this guide](Configuring-Monitoring-Ehcache.html)  
| MongoDb        | [this guide](Configuring-Monitoring-MongoDb.html)  
| JDBC           | [this guide](Configuring-Monitoring-JDBC.html)  
| LDAP           | [this guide](Configuring-Monitoring-LDAP.html)
