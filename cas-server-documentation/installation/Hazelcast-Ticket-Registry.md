---
layout: default
title: CAS - Hazelcast Ticket Registry
---

# Hazelcast Ticket Registry

Hazelcast Ticket Registry is a distributed ticket registry implementation based on [Hazelcast distributed grid library](http://hazelcast.org/)

This ticket registry implementation is enabled by simply including the module in the Maven pom like so:

{% highlight xml %}
<dependency>
    <groupId>org.jasig.cas</groupId>
    <artifactId>cas-server-integration-hazelcast</artifactId>
    <version>${cas.version}</version>
</dependency>
{% endhighlight %}

# Configuration

This implementation auto-configures most of the internal details of the underlying Hazelcast instance and the distributed `IMap` for tickets storage.
The only required configuration value on each CAS node in the cluster is a comma-separated list of all the member nodes defined in the configuration 
property `hz.cluster.members` (in `cas.properties` file). For example: `hz.cluster.members=cas1.example.com,cas2.example.com`

Other optional properties that could be set are:

* `hz.cluster.port` (default value is `5701`)
* `hz.cluster.portAutoIncrement` (default value is `true`)
* TGT time to live value for this implementation is set via `tgt.maxTimeToLiveInSeconds` and defaults to `28800`
* ST time to live value for this implementation is set via `st.timeToKillInSeconds` and defaults to `10`
  
  