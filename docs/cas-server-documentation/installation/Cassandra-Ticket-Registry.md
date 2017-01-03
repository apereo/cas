---
layout: default
title: CAS - Cassandra Ticket Registry
---

# Cassandra Ticket Registry
Cassandra integration is enabled by including the following dependency in the WAR overlay:

```xml
<dependency>
    <groupId>org.apereo.cas</groupId>
    <artifactId>cas-server-support-cassandra-ticket-registry</artifactId>
    <version>${cas.version}</version>
</dependency>
```
`NoSqlTicketRegistry` stores tickets in a one or more [memcached](http://memcached.org/) instances. The
[spymemcached](https://code.google.com/p/spymemcached/) library used by this component presents memcached as a
key/value store that accepts `String` keys and Java `Object` values.
Memcached stores data in exactly one node among many in a distributed cache, thus avoiding the requirement to replicate
or otherwise share data between nodes. A deterministic function is used to locate the node, _N'_, on which to store
key _K_:

    N' = f(h(K), N1, N2, N3, ... Nm)

where _h(K)_ is the hash of key _K_, _N1 ... Nm_ is the set of cache nodes, and _N'_ ∈ _N ... Nm_.

The function is deterministic in that it consistently produces the same result for a given key and set of cache nodes.
Note that a change in the set of available cache nodes may produce a different target node on which to store the key.

## Configuration Considerations

There are three core configuration concerns with memcached:

1. Hash Algorithm
2. Node locator strategy
3. Object serialization mechanism


### Object Serialization
Our Cassandra ticket registry implementation can store tickets as String or bytes of data, so CAS tickets must be serialized to a byte array prior to storage. 
CAS ships with two custom serialization components `JacksonBinarySerializer` and `JacksonJsonSerializer`. By default `JacksonJsonSerializer` is used, but you 
can use the other passing it to `CassandraDao`. 


## Configuration

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html).

## High Availability Considerations
