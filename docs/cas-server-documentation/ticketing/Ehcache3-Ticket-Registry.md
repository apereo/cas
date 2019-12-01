---
layout: default
title: CAS - Ehcache 3 Ticket Registry
category: Ticketing
---

# Ehcache3 Ticket Registry
Ehcache3 integration is enabled by including the following dependency in the WAR overlay:

```xml
<dependency>
     <groupId>org.apereo.cas</groupId>
     <artifactId>cas-server-support-ehcache3-ticket-registry</artifactId>
     <version>${cas.version}</version>
</dependency>
```

This registry stores tickets using the [Ehcache 3.x](http://ehcache.org/) caching library and an optional Terracotta cluster.

## In-memory store with disk persistence
Ehcache 3.x doesn't support distributing caching without Terracotta so using it without pointing at a Terracotta 
server or cluster doesn't support using more than one CAS server at a time, but the registry should survive restarts due 
to the disk persistence.

### Terracotta Clustering
By pointing this Ehcache module at a Terracotta server then multiple CAS servers can share tickets. CAS uses `autocreate` 
to create the Terracotta cluster configuration. If that isn't appropriate  

#### Configuration

To see the relevant list of CAS properties, please [review this guide](../configuration/Configuration-Properties.html#ehcache3-ticket-registry).
CAS currently doesn't support or require an XML configuration to configure Ehcache. 

### Eviction Policy

Ehcache can be configured as "eternal" in which case CAS's regular cleaning process will remove expired tickets. If the 
eternal property is set to false then storage timeouts will be set based on the metadata for the individual caches.  
