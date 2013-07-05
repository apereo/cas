---
layout: default
title: CAS - Ehcache Ticket Registry
---
# Ehcache Ticket Registry
Ehcache integration is enabled by including the following dependency in the Maven WAR overlay:

    <dependency>
         <groupId>org.jasig.cas</groupId>
         <artifactId>cas-server-integration-ehcache</artifactId>
         <version>${cas.version}</version>
    </dependency>

`EhCacheTicketRegistry` stores tickets in an [Ehcache](http://ehcache.org/) instance.

We present two configurations:

1. Single instance memory-backed cache with disk overflow for simple cases.
2. Distributed cache with peer replication over RMI for HA deployments.

## Memory Cache with Disk Overflow
The following Spring configuration provides a template for `ticketRegistry.xml`.
{% highlight xml %}
<bean id="ticketRegistry"
      class="org.jasig.cas.ticket.registry.EhCacheTicketRegistry"
      p:serviceTicketsCache-ref="serviceTicketsCache"
      p:ticketGrantingTicketsCache-ref="ticketGrantingTicketsCache" />

<bean id="abstractTicketCache" abstract="true"
      class="org.springframework.cache.ehcache.EhCacheFactoryBean"
      p:cacheManager-ref="cacheManager"
      p:diskExpiryThreadIntervalSeconds="0"
      p:diskPersistent="false"
      p:eternal="false"
      p:maxElementsInMemory="10000"
      p:maxElementsOnDisk="20000"
      p:memoryStoreEvictionPolicy="LRU"
      p:overflowToDisk="true" />

<bean id="serviceTicketsCache"
      class="org.springframework.cache.ehcache.EhCacheFactoryBean"
      parent="abstractTicketCache"
      p:cacheName="cas_st"
      p:timeToIdle="0"
      p:timeToLive="300" />

<bean id="ticketGrantingTicketsCache"
      class="org.springframework.cache.ehcache.EhCacheFactoryBean"
      p:cacheName="cas_tgt"
      p:timeToIdle="0"
      p:timeToLive="7201" />

<bean id="cacheManager"
      class="org.springframework.cache.ehcache.EhCacheManagerFactoryBean"
      p:configLocation="classpath:ehcache-failsafe.xml"
      p:shared="false"
      p:cacheManagerName="ticketRegistryCacheManager" />
{% endhighlight %}

The Ehcache configuration file `ehcache-failsafe.xml` mentioned in the Spring configuration above:
{% highlight xml %}
<ehcache updateCheck="false"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:noNamespaceSchemaLocation="http://ehcache.org/ehcache.xsd">

  <diskStore path="java.io.tmpdir/cas"/>

</ehcache>
{% endhighlight %}

## Distributed Cache with RMI Replication
Ehcache supports [RMI](http://docs.oracle.com/javase/6/docs/technotes/guides/rmi/index.html) replication for
distributed caches composed of two or more nodes. Distributed caches are recommended for HA architectures since they
offer fault tolerance in the ticket storage subsystem.

Spring configuration template for `ticketRegistry.xml`.
{% highlight xml %}
<bean id="ticketRegistry"
      class="org.jasig.cas.ticket.registry.EhCacheTicketRegistry"
      p:serviceTicketsCache-ref="serviceTicketsCache"
      p:ticketGrantingTicketsCache-ref="ticketGrantingTicketsCache" />

<bean id="abstractTicketCache" abstract="true"
      class="org.springframework.cache.ehcache.EhCacheFactoryBean"
      p:cacheManager-ref="cacheManager"
      p:diskExpiryThreadIntervalSeconds="0"
      p:diskPersistent="false"
      p:eternal="false"
      p:maxElementsInMemory="10000"
      p:maxElementsOnDisk="20000"
      p:memoryStoreEvictionPolicy="LRU"
      p:overflowToDisk="true"
      p:bootstrapCacheLoader-ref="ticketCacheBootstrapCacheLoader" />

<!-- MUST use synchronous repl for service tickets for correct behavior. -->
<bean id="serviceTicketsCache"
      class="org.springframework.cache.ehcache.EhCacheFactoryBean"
      parent="abstractTicketCache"
      p:cacheName="cas_st"
      p:timeToIdle="0"
      p:timeToLive="300"
      p:cacheEventListeners-ref="ticketRMISynchronousCacheReplicator" />

<bean id="ticketGrantingTicketsCache"
      class="org.springframework.cache.ehcache.EhCacheFactoryBean"
      p:cacheName="cas_tgt"
      p:timeToIdle="0"
      p:timeToLive="7201"
      p:cacheEventListeners-ref="ticketRMIAsynchronousCacheReplicator" />

<bean id="cacheManager"
      class="org.springframework.cache.ehcache.EhCacheManagerFactoryBean"
      p:configLocation="classpath:ehcache-replicated.xml"
      p:shared="false"
      p:cacheManagerName="ticketRegistryCacheManager" />

<bean id="ticketRMISynchronousCacheReplicator"
      class="net.sf.ehcache.distribution.RMISynchronousCacheReplicator"
      c:replicatePuts="true"
      c:replicatePutsViaCopy="true"
      c:replicateUpdates="true"
      c:replicateUpdatesViaCopy="true"
      c:replicateRemovals="true" />

<bean id="ticketRMIAsynchronousCacheReplicator"
      class="net.sf.ehcache.distribution.RMIAsynchronousCacheReplicator"
      parent="ticketRMISynchronousCacheReplicator"
      c:replicationInterval="10000"
      c:maximumBatchSize="100" />

<bean id="ticketCacheBootstrapCacheLoader"
      class="net.sf.ehcache.distribution.RMIBootstrapCacheLoader"
      c:asynchronous="true"
      c:maximumChunkSize="5000000" />
{% endhighlight %}

The Ehcache configuration for `ehcache-replicated.xml` mentioned in the Spring config follows.
{% highlight xml %}
<ehcache name="ehCacheTicketRegistryCache"
    updateCheck="false"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:noNamespaceSchemaLocation="http://ehcache.org/ehcache.xsd">

  <diskStore path="java.io.tmpdir/cas"/>

  <!--
     | Automatic peer discovery
     | See http://ehcache.org/documentation/user-guide/rmi-replicated-caching#automatic-peer-discovery
     | for more information.
     -->
  <!--
  <cacheManagerPeerProviderFactory
        class="net.sf.ehcache.distribution.RMICacheManagerPeerProviderFactory"
        properties="peerDiscovery=automatic, multicastGroupAddress=230.0.0.1, multicastGroupPort=4446, timeToLive=32"
        propertySeparator="," />
  -->

  <!--
     | Manual peer discovery
     | See http://ehcache.org/documentation/user-guide/rmi-replicated-caching#manual-peer-discovery-manual-peer-discovery
     | for more information
     -->
  <cacheManagerPeerProviderFactory
      class="net.sf.ehcache.distribution.RMICacheManagerPeerProviderFactory"
      properties="peerDiscovery=manual,rmiUrls=//peer-2:41001/cas_st|//peer-3:41001/cas_st|//peer-2:41001/cas_tgt|//peer-3:41001/cas_tgt" />
  <cacheManagerPeerListenerFactory
      class="net.sf.ehcache.distribution.RMICacheManagerPeerListenerFactory"
      properties="port=41001,remoteObjectPort=41002" />
</ehcache>
{% endhighlight %}

Use either manual or automatic peer discovery to assemble members of distributed cache. If manual discover is used
for configuration, the file would vary according to the node on which CAS is deployed. For that reason it may be
helpful to place the configuration file on the filesystem at a well-known location and reference it in the
`EhCacheManagerFactoryBean` above as follows:
{% highlight xml %}
<bean id="cacheManager"
      class="org.springframework.cache.ehcache.EhCacheManagerFactoryBean"
      p:configLocation="file:/path/to/well-known/ehcache-replicated.xml"
      p:shared="false"
      p:cacheManagerName="ticketRegistryCacheManager" />
{% endhighlight %}
