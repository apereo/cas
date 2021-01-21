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

{% include casmodule.html group="org.apereo.cas" module="cas-server-core-monitor" %}

{% include casproperties.html 
properties="cas.monitor.st,cas.monitor.tgt,cas.monitor.load,cas.monitor.memory" %}

<div class="alert alert-warning"><strong>YMMV</strong><p>In order to accurately and reliably 
report on ticket statistics, you are at the mercy of the underlying ticket registry to support 
the behavior in a performant manner which means that the infrastructure and network capabilities 
and latencies must be considered and carefully tuned. This might have become specially relevant 
in clustered deployments as depending on the ticket registry of choice, CAS may need 
to <i>interrogate</i> the entire cluster by running distributed queries to calculate ticket usage.</p></div>

## Memcached

Please [see this guide](Configuring-Monitoring-Memcached.html) for more info.

## Ehcache

Please [see this guide](Configuring-Monitoring-Ehcache.html) for more info.

## MongoDb

Please [see this guide](Configuring-Monitoring-MongoDb.html) for more info.

## Hazelcast

Please [see this guide](Configuring-Monitoring-Hazelcast.html) for more info.

## JDBC

Please [see this guide](Configuring-Monitoring-JDBC.html) for more info.

## LDAP

Please [see this guide](Configuring-Monitoring-LDAP.html) for more info.

