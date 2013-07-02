---
layout: default
title: CAS - Configuring Ticketing Components
---
# Configuring Ticketing Components
There are two core configurable ticketing components:
* `TicketRegistry` - Provides for durable ticket storage.
* `ExpirationPolicy` - Provides a policy framework for ticket expiration semantics.

## Ticket Registry
The deployment environment and technology expertise generally determine the particular `TicketRegistry` component.
A cache-backed implementation is recommended for HA deployments, while the default `DefaultTicketRegistry` in-memory
component may be suitable for small deployments.

### Default (In-Memory)
`DefaultTicketRegistry` uses a `ConcurrentHashMap` for memory-backed ticket storage and retrieval.
This component does not preserve ticket state across restarts. There are a few configuration knobs available:

* `initialCapacity` - `ConcurrentHashMap` initial capacity.
* `loadFactor` - `ConcurrentHashMap` load factor.
for more information.
* `concurrencyLevel` - Allows tuning the `ConcurrentHashMap` for concurrent write support.

All three arguments map to those of the [`ConcurrentHashMap` constructor](http://goo.gl/qKKg7).
{% highlight xml %}
<bean id="ticketRegistry"
      class="org.jasig.cas.ticket.registry.DefaultTicketRegistry"
      c:initialCapacity="10000"
      c:loadFactor="1"
      c:concurrencyLevel="20" />
{% endhighlight %}

### Ehcache
Ehcache integration is enabled by including the following dependency in the Maven WAR overlay:

    <dependency>
         <groupId>org.jasig.cas</groupId>
         <artifactId>cas-server-integration-ehcache</artifactId>
         <version>${cas.version}</version>
    </dependency>

`EhCacheTicketRegistry` stores tickets in an [Ehcache](http://ehcache.org/) instance.

We present two configurations:

1. A simple memory-backed cache with disk overflow for simple cases.
2. Cache with peer replication over RMI for HA deployments.

#### Memory with Disk Overflow
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

#### RMI Replication
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
Use either manual or automatic peer discovery to assemble members of distributed cache. If manual discover is used
for configuration, the file would vary according to the node on which CAS is deployed. For that reason it may be
helpful to place the configuration file on the filesystem at a well-known location and reference it in the
`EhCacheManagerFactoryBean` above as follows:

      <bean id="cacheManager"
            class="org.springframework.cache.ehcache.EhCacheManagerFactoryBean"
            p:configLocation="file:/path/to/well-known/ehcache-replicated.xml"
            p:shared="false"
            p:cacheManagerName="ticketRegistryCacheManager" />

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

### JBoss Cache
JBoss integration is enabled by including the following dependency in the Maven WAR overlay:

    <dependency>
         <groupId>org.jasig.cas</groupId>
         <artifactId>cas-server-integration-jboss</artifactId>
         <version>${cas.version}</version>
    </dependency>

`JBossCacheTicketRegistry` stores data in a [JBoss Cache](http://www.jboss.org/jbosscache/) that supports distributed
caches for failover support and is therefore suitable for HA deployments. Almost all of the configuration for this
component happens in the JBoss Cache configuration file; we present a starting configuration here but readers should
consult JBoss Cache
[configuration documentation](http://docs.jboss.org/jbosscache/3.2.1.GA/userguide_en/html/configuration.html)
for further details.


Sample `JBossCacheTicketRegistry` configuration for `ticketRegistry.xml`.
{% highlight xml %}
<bean id="ticketRegistry"
      class="org.jasig.cas.ticket.registry.JBossCacheTicketRegistry"
      p:cache-ref="cache" />
<bean id="cache"
      class="org.jasig.cas.util.JBossCacheFactoryBean"
      p:configLocation="classpath:jbossCache.xml" />
  </bean>
{% endhighlight %}

The `jbossCache.xml` file may be bundled in the overlay which allows it to be accessed from the classpath, as in the
example above, or it may be located on the filesystem. A sample `jbossCache.xml` file follows.
{% highlight xml %}
<?xml version="1.0" encoding="UTF-8"?>
<server>
  <mbean code="org.jboss.cache.TreeCache" name="jboss.cache:service=TreeCache">

    <depends>jboss:service=Naming</depends>
    <depends>jboss:service=TransactionManager</depends>

    <attribute name="TransactionManagerLookupClass">
    org.jboss.cache.transaction.DummyTransactionManagerLookup
    </attribute>

    <!--
       | SERIALIZABLE, REPEATABLE_READ (default), READ_COMMITTED, READ_UNCOMMITTED, NONE
       -->
    <attribute name="IsolationLevel">REPEATABLE_READ</attribute>

    <!--
       | LOCAL, REPL_ASYNC, REPL_SYNC, INVALIDATION_ASYNC, INVALIDATION_SYNC
       -->
    <attribute name="CacheMode">REPL_SYNC</attribute>

    <!-- Just used for async repl: use a replication queue -->
    <attribute name="UseReplQueue">false</attribute>

    <!-- Replication interval for replication queue (in ms) -->
    <attribute name="ReplQueueInterval">0</attribute>

    <!-- Max number of elements which trigger replication -->
    <attribute name="ReplQueueMaxElements">0</attribute>

    <!-- Cluster name must be same for all members in cluster in order to find each other. -->
    <attribute name="ClusterName">TreeCache-Cluster</attribute>

    <attribute name="ClusterConfig">
      <config>
        <!--
           | If you have a multihomed machine, set the bind_addr, e.g bind_addr="192.168.0.2"
           -->
        <UDP mcast_addr="228.1.2.3" mcast_port="48866"
          ip_ttl="64" ip_mcast="true"
          mcast_send_buf_size="150000" mcast_recv_buf_size="80000"
          ucast_send_buf_size="150000" ucast_recv_buf_size="80000"
          loopback="false"/>
        <PING timeout="2000" num_initial_members="3" up_thread="false" down_thread="false"/>
        <MERGE2 min_interval="10000" max_interval="20000"/>
        <FD_SOCK/>
        <VERIFY_SUSPECT timeout="1500" up_thread="false" down_thread="false"/>
        <pbcast.NAKACK gc_lag="50" retransmit_timeout="600,1200,2400,4800"
          max_xmit_size="8192" up_thread="false" down_thread="false"/>
        <UNICAST timeout="600,1200,2400" window_size="100" min_threshold="10" down_thread="false"/>
        <pbcast.STABLE desired_avg_gossip="20000" up_thread="false" down_thread="false"/>
        <FRAG frag_size="8192" down_thread="false" up_thread="false"/>
        <pbcast.GMS join_timeout="5000" join_retry_timeout="2000" shun="true" print_local_addr="true"/>
        <pbcast.STATE_TRANSFER up_thread="true" down_thread="true"/>
      </config>
    </attribute>


    <!-- Whether or not to fetch state on joining a cluster. -->
    <attribute name="FetchInMemoryState">true</attribute>

    <!--
       | 
       | The max amount of time (in milliseconds) we wait until the
       | initial state (ie. the contents of the cache) are retrieved from
       | existing members in a clustered environment
       -->
    <attribute name="InitialStateRetrievalTimeout">15000</attribute>

    <!--
       | Number of milliseconds to wait until all responses for a
       | synchronous call have been received.
       -->
    <attribute name="SyncReplTimeout">15000</attribute>

    <!-- Max number of milliseconds to wait for a lock acquisition -->
    <attribute name="LockAcquisitionTimeout">10000</attribute>

    <!-- Name of the eviction policy class. -->
    <attribute name="EvictionPolicyClass"></attribute>

    <attribute name="UseMarshalling">false</attribute>
    <attribute name="StateTransferVersion">130</attribute>
  </mbean>
</server>
{% endhighlight %}

### Memcached
Memcached integration is enabled by including the following dependency in the Maven WAR overlay:

    <dependency>
        <groupId>org.jasig.cas</groupId>
        <artifactId>cas-server-integration-memcached</artifactId>
        <version>${cas.version}</version>
    </dependency>

`MemCacheTicketRegistry` stores tickets in one or more [memcached](http://memcached.org/) instances. The
[spymemcached](https://code.google.com/p/spymemcached/) library used by this component presents memcached as a
key/value store that accepts `String` keys and Java `Object` values.
Memcached stores data in exactly one node among many in a distributed cache, thus avoiding the requirement to replicate
or otherwise share data between nodes. A deterministic function is used to locate the node, _N'_, on which to store
key _K_:

    N' = f(K, N1, N2, N3, ... Nm)

where _N1 ... Nm_ is the set of cache nodes and _N'_ ∈ _N ... Nm_.

The function is deterministic in that it consistently produces the same result for a given key and set of cache nodes.
Note that a change in the set of available cache nodes may produce a different target node on which to store the key.

There are three core configuration concerns with memcached:

1. Hash Algorithm
2. Node locator strategy
3. Object serialization mechanism

#### Hash Algorithm
The hash algorithm is used to transform a key value into a memcached storage key that uniquely identifies the
corresponding value. The choice of hashing algorithm has implications for failover behavior that is important
for HA deployments. The `FNV1_64_HASH` algorithm is recommended since it offers a nice balance of speed and low
collision rate; see the
[javadocs](https://github.com/couchbase/spymemcached/blob/2.8.1/src/main/java/net/spy/memcached/DefaultHashAlgorithm.java)
for alternatives. 

#### Node Locator
The node locator serves as the deterministic node selection function for the memcached client provided by the
underlying spymemcached library. There are two choices:

1. [ARRAY_MOD](https://github.com/couchbase/spymemcached/blob/2.8.1/src/main/java/net/spy/memcached/ArrayModNodeLocator.java)
2. [CONSISTENT](https://github.com/couchbase/spymemcached/blob/2.9.0/src/main/java/net/spy/memcached/KetamaNodeLocator.java)

The array modulus mechanism is the default and suitable for cases when the number of nodes in the memcached pool is
expected to be consistent. The algorithm simply computes an index into the array of memcached nodes:

    hash(key) % length(nodes)

Obviously the selected index is a function of the number of memcached nodes, so variance in number of nodes produces
variance in the node selected to store the key, which is undesirable.

The consistent strategy generally provides a target node that does not vary with the number of nodes. This strategy
should be used in cases where the memcached pool may grow or shrink dynamically, including due to frequent node
failure.

#### Object Serialization
Memcached stores bytes of data, so CAS tickets must be serialized to a byte array prior to storage. CAS ships with
a custom serialization component `KryoTranscoder` based on the [Kryo](https://code.google.com/p/kryo/) serialization
framework. This component is recommended over the default Java serialization mechanism since it produces much more
compact data, which benefits both storage requirements and throughput.

#### Configuration
The following configuration is a template for `ticketRegistry.xml` Spring configuration:
{% highlight xml %}
<bean id="ticketRegistry"
      class="org.jasig.cas.ticket.registry.MemCacheTicketRegistry"
      p:client-ref="memcachedClient"
      p:ticketGrantingTicketTimeOut="${expiration.policy.tgt.validity_period}"
      p:serviceTicketTimeOut="${expiration.policy.st.validity_period}" />

<bean id="memcachedClient" class="net.spy.memcached.spring.MemcachedClientFactoryBean"
      p:servers="${memcached.servers}"
      p:protocol="${memcached.protocol}"
      p:locatorType="${memcached.locatorType}"
      p:failureMode="${memcached.failureMode}"
      p:transcoder-ref="kryoTranscoder">
  <property name="hashAlg">
    <util:constant static-field="net.spy.memcached.DefaultHashAlgorithm.${memcached.hashAlgorithm}" />
  </property>
</bean>

<bean id="kryoTranscoder"
      class="org.jasig.cas.ticket.registry.support.kryo.KryoTranscoder"
      init-method="initialize"
      initialBufferSize="8192" />
{% endhighlight %}

`MemCacheTicketRegistry` properties reference:

    expiration.policy.tgt.validity_period=7201
    expiration.policy.st.validity_period=5
    memcached.servers=cas-1.example.org:11211,cas-2.example.org:11211,cas-3.example.org:11211
    memcached.hashAlgorithm=FNV1_64_HASH
    memcached.protocol=BINARY
    memcached.locatorType=ARRAY_MOD
    memcached.failureMode=Redistribute
    memcached.transcoder.initBufSize=12288

#### High Availability Considerations
Memcached does not provide for replication by design, but the client is tolerant to node failures with
`failureMode="Redistribute"`. In this mode, a write failure will simply cause the client to rekey the item and write
it to an available node. It will continue to read and write from a backup node until the failed node returns, at
which time if the key is still available in the resurrected node it will supercede the value known to the backup node.
The most relevant consequence is that the set of services accessed during the SSO session may have two distinct values,
which may affect single sign-out. If possible the memcached service on the failed node should be restarted prior to
rejoining the cache pool to avoid this behavior.

