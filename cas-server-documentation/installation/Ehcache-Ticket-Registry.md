---
layout: default
title: CAS - Ehcache Ticket Registry
---

# Ehcache Ticket Registry
Ehcache integration is enabled by including the following dependency in the Maven WAR overlay:

```xml
<dependency>
     <groupId>org.jasig.cas</groupId>
     <artifactId>cas-server-integration-ehcache</artifactId>
     <version>${cas.version}</version>
</dependency>
```

`EhCacheTicketRegistry` stores tickets in an [Ehcache](http://ehcache.org/) instance.


## Distributed Cache
Distributed caches are recommended for HA architectures since they offer fault tolerance in the ticket storage
subsystem.


### RMI Replication
Ehcache supports [RMI](http://docs.oracle.com/javase/6/docs/technotes/guides/rmi/index.html)
replication for distributed caches composed of two or more nodes. To learn more about RMI
replication with Ehcache, [see this resource](http://ehcache.org/documentation/user-guide/rmi-replicated-caching).

Enable the registry via:

```xml
<alias name="ehcacheTicketRegistry" alias="ticketRegistry" />
```

#### Configuration
```properties
# ehcache.config.file=classpath:ehcache-replicated.xml
# ehcache.cachemanager.shared=false
# ehcache.cachemanager.name=ticketRegistryCacheManager
# ehcache.disk.expiry.interval.seconds=0
# ehcache.disk.persistent=false
# ehcache.eternal=false
# ehcache.max.elements.memory=10000
# ehcache.max.elements.disk=0
# ehcache.eviction.policy=LRU
# ehcache.overflow.disk=false
# ehcache.cache.loader.async=true
# ehcache.cache.loader.chunksize=5000000
# ehcache.repl.async.interval=10000
# ehcache.repl.async.batch.size=100
# ehcache.repl.sync.puts=true
# ehcache.repl.sync.putscopy=true
# ehcache.repl.sync.updates=true
# ehcache.repl.sync.updatescopy=true
# ehcache.repl.sync.removals=true
# ehcache.cache.name=org.jasig.cas.ticket.ServiceTicket
# ehcache.cache.timeIdle=0
# ehcache.cache.timeAlive=9000
```

The Ehcache configuration for `ehcache-replicated.xml` mentioned in the config follows.

```xml

<ehcache name="ehCacheTicketRegistryCache"
    updateCheck="false"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:noNamespaceSchemaLocation="http://ehcache.org/ehcache.xsd">

        <diskStore path="java.io.tmpdir/cas"/>

        <!-- Automatic Peer Discovery
        <cacheManagerPeerProviderFactory
        class="net.sf.ehcache.distribution.RMICacheManagerPeerProviderFactory"
        properties="peerDiscovery=automatic, multicastGroupAddress=230.0.0.1, multicastGroupPort=4446, timeToLive=32"
        propertySeparator="," />
        -->

        <!-- Manual Peer Discovery -->
        <cacheManagerPeerProviderFactory
            class="net.sf.ehcache.distribution.RMICacheManagerPeerProviderFactory"
            properties="peerDiscovery=manual,rmiUrls=//localhost:41001/org.jasig.cas.ticket.TicketCache" />
        <cacheManagerPeerListenerFactory
            class="net.sf.ehcache.distribution.RMICacheManagerPeerListenerFactory"
            properties="port=41001,remoteObjectPort=41002" />
</ehcache>
```

### Eviction Policy
Ehcache manages the internal eviction policy of cached objects via `timeToIdle` and `timeToLive` settings.
These settings control the general policy of the cache that is used to store various ticket types. In general,
you need to ensure the cache is alive long enough to support the individual expiration policy of tickets, and let
CAS clean the tickets as part of its own cleaner. 


### Troubleshooting Guidelines

* You will need to ensure that network communication across CAS nodes is allowed and no firewall or other component
 is blocking traffic.

* If you are running this on a server with active firewalls, you will probably need to specify
a fixed `remoteObjectPort`, within the `cacheManagerPeerListenerFactory`.
* Depending on environment settings and version of Ehcache used, you may also have to adjust the
`shared` setting .
* Ensure that each cache manager specified a name that matches the Ehcache configuration itself.
* You may also need to adjust your expiration policy to allow for a larger time span, specially
for service tickets depending on network traffic and communication delay across CAS nodes particularly
in the event that a node is trying to join the cluster.
