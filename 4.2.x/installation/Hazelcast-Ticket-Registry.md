---
layout: default
title: CAS - Hazelcast Ticket Registry
---

# Hazelcast Ticket Registry

Hazelcast Ticket Registry is a distributed ticket registry implementation based on [Hazelcast distributed grid library](http://hazelcast.org/). The registry implementation is cluster-aware and is able to auto-join a cluster of all the CAS nodes that expose this registry. Hazelcast will use port auto-increment feature to assign a TCP port to each member of a cluster starting from initially provided arbitrary port (`5701` by default).

Hazelcast will evenly distribute the ticket data among all the members of a cluster in a very efficient manner. Also, by default, the data collection on each node is configured with 1 backup copy, so that Hazelcast will use it to make strong data consistency guarantees i.e. the loss of data on live nodes will not occur should any other *primary data owner* members die. The data will be re-partitioned among the remaining live cluster members.

This ticket registry implementation is enabled by simply including the module in the Maven overlay pom:

{% highlight xml %}
<dependency>
    <groupId>org.jasig.cas</groupId>
    <artifactId>cas-server-integration-hazelcast</artifactId>
    <version>${cas.version}</version>
</dependency>
{% endhighlight %}

## Configuration

Enable the registry via:

{% highlight xml %}
<alias name="hazelcastTicketRegistry" alias="ticketRegistry" />
{% endhighlight %}

This implementation auto-configures most of the internal details of the underlying Hazelcast instance and
the distributed `IMap` for tickets storage.

{% highlight properties %}
# hz.cluster.portAutoIncrement=true
# hz.cluster.port=5701
# hz.cluster.multicast.enabled=false
# hz.cluster.members=cas1.example.com,cas2.example.com
# hz.cluster.tcpip.enabled=true
# hz.cluster.max.heapsize.percentage=85
# hz.cluster.max.heartbeat.seconds=5
# hz.cluster.eviction.percentage=10
# hz.cluster.eviction.policy=LRU
# hz.cluster.instance.name=${host.name}
{% endhighlight %}

## Logging
To enable additional logging for the registry, configure the log4j configuration file to add the following
levels:

{% highlight xml %}
...
<AsyncLogger name="com.hazelcast" level="debug" additivity="false">
    <AppenderRef ref="console"/>
    <AppenderRef ref="file"/>
</AsyncLogger>
...
{% endhighlight %}
