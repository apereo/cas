# Memcached Ticket Registry

Source: https://apereo.github.io/cas/development/ticketing/Memcached-Ticket-Registry.html

Memcached integration is enabled by including the following dependency in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-memcached-ticket-registry"
}
```

> **Usage**
>
> **This feature is deprecated and is scheduled to be removed in the future.**

This registry stores tickets in one or more [memcached](http://memcached.org/) instances. Memcached stores data in exactly one node among many in a distributed cache, thus avoiding the requirement to replicate or otherwise share data between nodes. A deterministic function is used to locate the node, *N’*, on which to store key *K*:

```
N' = f(h(K), N1, N2, N3, ... Nm)
```

where *h(K)* is the hash of key *K*, *N1 … Nm* is the set of cache nodes, and *N’* ∈ *N … Nm*.

The function is deterministic in that it consistently produces the same result for a given key and set of cache nodes. Note that a change in the set of available cache nodes may produce a different target node on which to store the key.

The actual memcached implementation may be supported via one of the following options, expected to be defined in the overlay.

> **Usage Warning!**
>
> Not all ticket registry operations are supported by the memcached ticket registry implementation. In particular, operations that execute bulk queries such as deleting and fetching all tickets in a single request may be unsupported, as memcached itself is rather unable to process and support that type of query.

## Spymemcached

Enable support via the spymemcached library. This is a simple, asynchronous, single-threaded memcached client that should be the default choice for the majority of deployments.

Support is enabled by including the following dependency in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-memcached-spy"
}
```

## AWS ElastiCache

You may also use [AWS ElastiCache](https://docs.aws.amazon.com/AmazonElastiCache/latest/dg/WhatIs.html) which is a web service that makes it easy to set up, manage, and scale a distributed in-memory data store or cache environment in the cloud. It provides a high-performance, scalable, and cost-effective caching solution, while removing the complexity associated with deploying and managing a distributed cache environment.

For clusters running the Memcached engine, ElastiCache supports Auto Discovery—the ability for client programs to automatically identify all of the nodes in a cache cluster, and to initiate and maintain connections to all of these nodes. With Auto Discovery, CAS does not need to manually connect to individual cache nodes; instead, CAS connects to one Memcached node and retrieves the list of nodes. From that list, CAS is aware of the rest of the nodes in the cluster and can connect to any of them. You do not need to hard code the individual cache node endpoints in the configuration

All of the cache nodes in the cluster maintain a list of metadata about all of the other nodes. This metadata is updated whenever nodes are added or removed from the cluster.

Support is enabled by including the following dependency in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-memcached-aws-elasticache"
}
```

## Configuration Considerations

There are three core configuration concerns with memcached:

1. Hash Algorithm
2. Node locator strategy
3. Object serialization mechanism

### Hash Algorithm

The hash algorithm is used to transform a key value into a memcached storage key that uniquely identifies the corresponding value. The choice of hashing algorithm has implications for failover behavior that is important for HA deployments. The `FNV1_64_HASH` algorithm is recommended since it offers a nice balance of speed and low collision rate; see the [javadocs](https://github.com/couchbase/spymemcached/blob/2.8.1/src/main/java/net/spy/memcached/DefaultHashAlgorithm.java) for alternatives.

### Node Locator

The node locator serves as the deterministic node selection function for the memcached client provided by the underlying spymemcached library. There are two choices:

1. [ARRAY_MOD](https://github.com/couchbase/spymemcached/blob/2.8.1/src/main/java/net/spy/memcached/ArrayModNodeLocator.java)
2. [CONSISTENT](https://github.com/couchbase/spymemcached/blob/2.9.0/src/main/java/net/spy/memcached/KetamaNodeLocator.java)

The array modulus mechanism is the default and suitable for cases when the number of nodes in the memcached pool is expected to be consistent. The algorithm computes an index into the array of memcached nodes:

```
hash(key) % length(nodes)
```

Obviously the selected index is a function of the number of memcached nodes, so variance in number of nodes produces variance in the node selected to store the key, which is undesirable.

The consistent strategy generally provides a target node that does not vary with the number of nodes. This strategy should be used in cases where the memcached pool may grow or shrink dynamically, including due to frequent node failure.

### Object Serialization

Memcached stores bytes of data, so CAS tickets must be serialized to a byte array prior to storage. CAS ships with a custom serialization component `KryoTranscoder` based on the [Kryo](https://github.com/EsotericSoftware/kryo) serialization framework. This component is recommended over the default Java serialization mechanism since it produces much more compact data, which benefits both storage requirements and throughput.

## Configuration

The following settings and properties are available from the CAS configuration catalog:

- `cas.ticket.registry.memcached.crypto.encryption.key` (Required; type `String`): The encryption key. The encryption key by default and unless specified otherwise must be randomly-generated string whose length is defined by the encryption key size setting.
- `cas.ticket.registry.memcached.crypto.signing.key` (Required; type `String`): The signing key is a string whose length is defined by the signing key size setting.
- `cas.ticket.registry.memcached.servers` (Required; type `String`; default `localhost:11211`): Comma-separated list of memcached servers.
- `cas.ticket.registry.memcached.crypto.alg` (Optional; type `String`; default `AES`): The signing/encryption algorithm to use.
- `cas.ticket.registry.memcached.crypto.enabled` (Optional; type `Boolean`; default `true`): Whether crypto operations are enabled.
- `cas.ticket.registry.memcached.crypto.encryption.key-size` (Optional; type `Integer`; default `16`): Encryption key size.
- `cas.ticket.registry.memcached.crypto.signing-enabled` (Optional; type `Boolean`; default `true`): Whether signing encryption operations are enabled.
- `cas.ticket.registry.memcached.crypto.signing.key-size` (Optional; type `Integer`; default `512`): The signing key size.
- `cas.ticket.registry.memcached.daemon` (Optional; type `Boolean`; default `true`): Set the daemon state of the IO thread (defaults to true).
- `cas.ticket.registry.memcached.failure-mode` (Optional; type `String`; default `Redistribute`): Failure mode. Acceptable values are `Redistribute,Retry,Cancel`.
- `cas.ticket.registry.memcached.hash-algorithm` (Optional; type `String`; default `FNV1_64_HASH`): Hash algorithm. Acceptable values are `NATIVE_HASH,CRC_HASH,FNV1_64_HASH,FNV1A_64_HASH,FNV1_32_HASH,FNV1A_32_HASH,KETAMA_HASH`.
- `cas.ticket.registry.memcached.kryo-auto-reset` (Optional; type `Boolean`; default `false`): If true, `reset` is called automatically after an entire object graph has been read or written. If false, `reset` must be called manually, which allows unregistered class names, references, and other information to span multiple object graphs.
- `cas.ticket.registry.memcached.kryo-objects-by-reference` (Optional; type `Boolean`; default `false`): If true, each appearance of an object in the graph after the first is stored as an integer ordinal. When set to true, `MapReferenceResolver` is used. This enables references to the same object and cyclic graphs to be serialized, but typically adds overhead of one byte per object.
- `cas.ticket.registry.memcached.kryo-registration-required` (Optional; type `Boolean`; default `true`): If true, an exception is thrown when an unregistered class is encountered. If false, when an unregistered class is encountered, its fully qualified class name will be serialized and the default serializer for the class used to serialize the object. Subsequent appearances of the class within the same object graph are serialized as an int id. Registered classes are serialized as an int id, avoiding the overhead of serializing the class name, but have the drawback of needing to know the classes to be serialized up front. See `ComponentSerializationPlan` for help here.
- `cas.ticket.registry.memcached.locator-type` (Optional; type `String`; default `ARRAY_MOD`): Locator mode. Acceptable values are `ARRAY_MOD, CONSISTENT, VBUCKET`.
- `cas.ticket.registry.memcached.max-idle` (Optional; type `Integer`; default `8`): Set the value for the maxTotal configuration attribute for pools created with this configuration instance.
- `cas.ticket.registry.memcached.max-reconnect-delay` (Optional; type `Long`; default `-1`): Set the maximum reconnect delay.
- `cas.ticket.registry.memcached.max-total` (Optional; type `Integer`; default `20`): Sets the cap on the number of objects that can be allocated by the pool (checked out to clients, or idle awaiting checkout) at a given time. Use a negative value for no limit.
- `cas.ticket.registry.memcached.min-idle` (Optional; type `Integer`; default `0`): Get the value for the minIdle configuration attribute for pools created with this configuration instance.
- `cas.ticket.registry.memcached.op-timeout` (Optional; type `Long`; default `-1`): Set the default operation timeout in milliseconds.
- `cas.ticket.registry.memcached.protocol` (Optional; type `String`; default `TEXT`): Protocol. Acceptable values are `TEXT, BINARY`.
- `cas.ticket.registry.memcached.should-optimize` (Optional; type `Boolean`; default `false`): Set to false if the default operation optimization is not desirable.
- `cas.ticket.registry.memcached.shutdown-timeout-seconds` (Optional; type `Long`; default `-1`): The number of seconds to wait for connections to finish before shutting down the client.
- `cas.ticket.registry.memcached.timeout-exception-threshold` (Optional; type `Integer`; default `2`): Set the maximum timeout exception threshold.
- `cas.ticket.registry.memcached.transcoder` (Optional; type `BaseMemcachedProperties.TranscoderTypes`; default `KRYO`): Indicate the transcoder type. Available values are as follows: - `KRYO`: CAS transcoder implementation based on Kryo fast serialization framework suited for efficient serialization of tickets. Provides pooling mechanisms as well as control over object registration and sequences. - `SERIAL`: Kryo native transcoder that serializes and compresses objects. - `WHALIN`: Transcoder that provides compatibility with Greg Whalin's memcached client. - `WHALINV1`: Handles old whalin encoding: data type is in the first byte of the value.
- `cas.ticket.registry.memcached.transcoder-compression-threshold` (Optional; type `Integer`; default `16384`): For transcoders other than kryo, determines the compression threshold. Does not apply to kryo.
- `cas.ticket.registry.memcached.use-nagle-algorithm` (Optional; type `Boolean`; default `false`): Set to true if you'd like to enable the Nagle algorithm.

**Signing & encryption**

This CAS feature is able to accept signing and encryption crypto keys. In most scenarios if keys are not provided, CAS will auto-generate them. The following instructions apply if you wish to manually and beforehand create the signing and encryption keys.

Note that if you are asked to create a [JWK](https://tools.ietf.org/html/rfc7517) of a certain size for the key, you are to use the following set of commands to generate the token:

```bash
wget https://raw.githubusercontent.com/apereo/cas/master/etc/jwk-gen.jar
java -jar jwk-gen.jar -t oct -s [size]
```

The outcome would be similar to:

```json
{
  "kty": "oct",
  "kid": "...",
  "k": "..."
}
```

The generated value for `k` needs to be assigned to the relevant CAS settings. Note that keys generated via the above algorithm are processed by CAS using the Advanced Encryption Standard (`AES`) algorithm which is a specification for the encryption of electronic data established by the U.S. National Institute of Standards and Technology.

---

## High Availability Considerations

Memcached does not provide for replication by design, but the client is tolerant to node failures with `failureMode="Redistribute"`. In this mode a write failure will cause the client to flag the node as failed and remove it from the set of available nodes. It subsequently recomputes the node location function with the reduced node set to find a new node on which to store the key. If the node location function selects the same node, which is likely for the *CONSISTENT* strategy, a backup node will be computed. The value is written to and read from the failover node until the primary node recovers. The client will periodically check the failed node for liveliness and restore it to the node pool as soon as it recovers. When the primary node is resurrected, if it contains a value for a particular key, it would supersede the value known to the failover node. The most common effect on CAS behavior in this circumstance would occur when ticket-granting tickets have duplicate values, which could affect single sign-out and prevent access to services. In particular, services accessed and forced authentications that occur while the failover service is active would be lost when the failed node recovers. In most cases this behavior is tolerable, but it can be avoided by restarting the memcached service on the failed node prior to rejoining the cache pool.

A read failure in *Redistribute* mode causes the node to be removed from the set of available nodes, a failover node is computed, and a value is read from that node. In most cases this results in a key not found situation. The effect on CAS behavior depends on the type of ticket requested:

- Service ticket - Service access would be denied for the requested ticket, but permitted for subsequent attempts since a new ticket would be generated and validated.
- Ticket-granting ticket - The SSO session would be terminated and re-authentication would be required.

Read failures are thus entirely innocuous for environments where re-authentication is acceptable.
