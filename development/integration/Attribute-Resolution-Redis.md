# Redis Attribute Resolution

Source: https://apereo.github.io/cas/development/integration/Attribute-Resolution-Redis.html

The following configuration describes how to fetch and retrieve attributes from Redis attribute repositories.

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-redis-authentication"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.attribute-repository.redis[0].cluster.nodes.host` (Required; type `String`): Server's host address.
- `cas.authn.attribute-repository.redis[0].cluster.nodes.port` (Required; type `int`): Server's port number.
- `cas.authn.attribute-repository.redis[0].cluster.nodes.replica-of` (Required; type `String`): Set the id of the master node.
- `cas.authn.attribute-repository.redis[0].cluster.nodes.type` (Required; type `String`): Indicate the type/role of this node. Accepted values are: `MASTER, REPLICA`.
- `cas.authn.attribute-repository.redis[0].cluster.nodes[0].host` (Required; type `String`): Server's host address.
- `cas.authn.attribute-repository.redis[0].cluster.nodes[0].port` (Required; type `int`): Server's port number.
- `cas.authn.attribute-repository.redis[0].cluster.nodes[0].replica-of` (Required; type `String`): Set the id of the master node.
- `cas.authn.attribute-repository.redis[0].cluster.nodes[0].type` (Required; type `String`): Indicate the type/role of this node. Accepted values are: `MASTER, REPLICA`.
- `cas.authn.attribute-repository.redis[0].cluster.password` (Required; type `String`): The cluster connection's password.
- `cas.authn.attribute-repository.redis[0].cluster.username` (Required; type `String`): The cluster connection's username.
- `cas.authn.attribute-repository.redis[0].database` (Required; type `int`): Database index used by the connection factory.
- `cas.authn.attribute-repository.redis[0].enabled` (Required; type `boolean`; default `true`): Whether the module is enabled or not, defaults to true.
- `cas.authn.attribute-repository.redis[0].host` (Required; type `String`; default `localhost`): Redis server host.
- `cas.authn.attribute-repository.redis[0].password` (Required; type `String`): Login password of the redis server.
- `cas.authn.attribute-repository.redis[0].pool.enabled` (Required; type `boolean`): Enable the pooling configuration.
- `cas.authn.attribute-repository.redis[0].port` (Required; type `int`; default `6379`): Redis server port.
- `cas.authn.attribute-repository.redis[0].sentinel.master` (Required; type `String`): Name of Redis server.
- `cas.authn.attribute-repository.redis[0].username` (Required; type `String`): Login username of the redis server.
- `cas.authn.attribute-repository.redis` (Optional; type `List<RedisPrincipalAttributesProperties>`): Retrieve attributes from redis repositories.
- `cas.authn.attribute-repository.redis[0].certificate-file` (Optional; type `File`): May be used when making SSL connections to build the trust manager. Sets the certificate file to use for client authentication. This is typically an `X.509` certificate file (or chain file) in PEM format.
- `cas.authn.attribute-repository.redis[0].cluster.dynamic-refresh-sources` (Optional; type `boolean`; default `true`): Whether to discover and query all cluster nodes for obtaining the cluster topology. When set to false, only the initial seed nodes are used as sources for topology discovery.
- `cas.authn.attribute-repository.redis[0].cluster.max-redirects` (Optional; type `int`): The max number of redirects to follow.
- `cas.authn.attribute-repository.redis[0].cluster.nodes.id` (Optional; type `String`): Identifier of this node.
- `cas.authn.attribute-repository.redis[0].cluster.nodes.name` (Optional; type `String`): Name of this node.
- `cas.authn.attribute-repository.redis[0].cluster.nodes[0].id` (Optional; type `String`): Identifier of this node.
- `cas.authn.attribute-repository.redis[0].cluster.nodes[0].name` (Optional; type `String`): Name of this node.
- `cas.authn.attribute-repository.redis[0].cluster.topology-refresh-period` (Optional; type `String`): Enables periodic refresh of cluster topology and sets the refresh period.
- `cas.authn.attribute-repository.redis[0].connect-timeout` (Optional; type `String`; default `PT10S`): Connection timeout.
- `cas.authn.attribute-repository.redis[0].id` (Optional; type `String`): A value can be assigned to this field to uniquely identify this resolver.
- `cas.authn.attribute-repository.redis[0].keep-alive-count` (Optional; type `int`): The maximum number of keepalive probes TCP should send before dropping the connection. By default, leaving this number at zero disables keepalive.
- `cas.authn.attribute-repository.redis[0].keep-alive-idle-timeout` (Optional; type `String`; default `PT2H`): The time the connection needs to remain idle before TCP starts sending keepalive probes if keepalive is enabled.
- `cas.authn.attribute-repository.redis[0].keep-alive-interval` (Optional; type `String`; default `PT60S`): The time the connection needs to remain idle before TCP starts sending keepalive probes.
- `cas.authn.attribute-repository.redis[0].key-certificate-chain-file` (Optional; type `File`): May be used when making SSL connections to build the key manager. Sets the key certificate file to use for client authentication. This is typically an `X.509` certificate file (or chain file) in PEM format.
- `cas.authn.attribute-repository.redis[0].key-file` (Optional; type `File`): May be used when making SSL connections. Sets the key file for client authentication. The key is reloaded on each connection attempt that allows CAS to replace certificates during runtime. This is typically a `PKCS#8` private key file in PEM format.
- `cas.authn.attribute-repository.redis[0].key-password` (Optional; type `String`): The password of the `#keyFile`, or `null` if it's not password-protected.
- `cas.authn.attribute-repository.redis[0].order` (Optional; type `int`): The order of this attribute repository in the chain of repositories. Can be used to explicitly position this source in chain and affects merging strategies.
- `cas.authn.attribute-repository.redis[0].pool.fairness` (Optional; type `boolean`): Returns whether or not the pool serves threads waiting to borrow objects fairly. True means that waiting threads are served as if waiting in a FIFO queue.
- `cas.authn.attribute-repository.redis[0].pool.lifo` (Optional; type `boolean`; default `true`): Returns whether the pool has LIFO (last in, first out) behaviour with respect to idle objects - always returning the most recently used object from the pool, or as a FIFO (first in, first out) queue, where the pool always returns the oldest object in the idle object pool.
- `cas.authn.attribute-repository.redis[0].pool.max-active` (Optional; type `int`; default `8`): Max number of connections that can be allocated by the pool at a given time. Use a negative value for no limit.
- `cas.authn.attribute-repository.redis[0].pool.max-idle` (Optional; type `int`; default `8`): Max number of "idle" connections in the pool. Use a negative value to indicate an unlimited number of idle connections.
- `cas.authn.attribute-repository.redis[0].pool.max-wait` (Optional; type `String`; default `PT5S`): Maximum amount of time (in milliseconds) a connection allocation should block before throwing an exception when the pool is exhausted. Use a negative value to block indefinitely.
- `cas.authn.attribute-repository.redis[0].pool.min-evictable-idle-time-millis` (Optional; type `long`): Sets the minimum amount of time an object may sit idle in the pool before it is eligible for eviction by the idle object evictor (if any - see setTimeBetweenEvictionRunsMillis(long)). When non-positive, no objects will be evicted from the pool due to idle time alone.
- `cas.authn.attribute-repository.redis[0].pool.min-idle` (Optional; type `int`): Target for the minimum number of idle connections to maintain in the pool. This setting only has an effect if it is positive.
- `cas.authn.attribute-repository.redis[0].pool.num-tests-per-eviction-run` (Optional; type `int`): Sets the maximum number of objects to examine during each run (if any) of the idle object evictor thread. When positive, the number of tests performed for a run will be the minimum of the configured value and the number of idle instances in the pool. When negative, the number of tests performed will be ceil(getNumIdle()/ abs(getNumTestsPerEvictionRun())) which means that when the value is -n roughly one nth of the idle objects will be tested per run.
- `cas.authn.attribute-repository.redis[0].pool.soft-min-evictable-idle-time-millis` (Optional; type `long`): Sets the minimum amount of time an object may sit idle in the pool before it is eligible for eviction by the idle object evictor (if any - see setTimeBetweenEvictionRunsMillis(long)), with the extra condition that at least minIdle object instances remain in the pool. This setting is overridden by getMinEvictableIdleTimeMillis() (that is, if getMinEvictableIdleTimeMillis() is positive, then getSoftMinEvictableIdleTimeMillis() is ignored).
- `cas.authn.attribute-repository.redis[0].pool.test-on-borrow` (Optional; type `boolean`): Returns whether objects borrowed from the pool will be validated before being returned from the borrowObject() method. Validation is performed by the validateObject() method of the factory associated with the pool. If the object fails to validate, it will be removed from the pool and destroyed, and a new attempt will be made to borrow an object from the pool.
- `cas.authn.attribute-repository.redis[0].pool.test-on-create` (Optional; type `boolean`): Returns whether objects created for the pool will be validated before being returned from the borrowObject() method. Validation is performed by the validateObject() method of the factory associated with the pool. If the object fails to validate, then borrowObject() will fail.
- `cas.authn.attribute-repository.redis[0].pool.test-on-return` (Optional; type `boolean`): Returns whether objects borrowed from the pool will be validated when they are returned to the pool via the returnObject() method. Validation is performed by the validateObject() method of the factory associated with the pool. Returning objects that fail validation are destroyed rather then being returned the pool.
- `cas.authn.attribute-repository.redis[0].pool.test-while-idle` (Optional; type `boolean`): Returns whether objects sitting idle in the pool will be validated by the idle object evictor ( if any - see setTimeBetweenEvictionRunsMillis(long)). Validation is performed by the validateObject() method of the factory associated with the pool. If the object fails to validate, it will be removed from the pool and destroyed.
- `cas.authn.attribute-repository.redis[0].pool.time-between-eviction-runs-millis` (Optional; type `long`; default `-1`): Sets the amount of time (in milliseconds) between runs of the idle object evictor thread. When non-positive, no idle object evictor thread will be run. If positive, the idle object evictor thread will run at least once every timeBetweenEvictionRunsMillis milliseconds, and will attempt to evict objects from the pool that are idle longer than getMinEvictableIdleTimeMillis() (if positive) or getSoftMinEvictableIdleTimeMillis() (if positive).
- `cas.authn.attribute-repository.redis[0].protocol-version` (Optional; type `String`; default `RESP3`): Redis protocol version.
- `cas.authn.attribute-repository.redis[0].read-from` (Optional; type `BaseRedisProperties.RedisReadFromTypes`): Setting that describes how Lettuce routes read operations to replica nodes. Note that modes referencing MASTER/SLAVE are deprecated (but still supported) in the Lettuce redis client dependency so migrate config to UPSTREAM/REPLICA. Available values are as follows: - `UPSTREAM`: Read from the current upstream node. - `UPSTREAMPREFERRED`: Read from the upstream node, but if it is unavailable, read from replica nodes. - `MASTER`: Read from the current upstream node. - `MASTERPREFERRED`: Read from the upstream node, but if it is unavailable, read from replica nodes. - `SLAVE`: Read from replica nodes. - `SLAVEPREFERRED`: Read from the replica nodes, but if none is unavailable, read from the upstream node. - `REPLICA`: Read from replica nodes. - `REPLICAPREFERRED`: Read from the replica nodes, but if none is unavailable, read from the upstream node. - `ANY`: Read from any node of the cluster. - `ANYREPLICA`: Read from any replica node of the cluster. - `NEAREST`: Read from the nearest node.
- `cas.authn.attribute-repository.redis[0].sentinel.node` (Optional; type `List<String>`): list of host:port pairs.
- `cas.authn.attribute-repository.redis[0].sentinel.password` (Optional; type `String`): Login password of the sentinel server.
- `cas.authn.attribute-repository.redis[0].share-native-connections` (Optional; type `Boolean`): The shared native connection is never closed by Lettuce connection, therefore it is not validated by default when connections are retrieved. If this setting is `true`, a shared connection will be used for regular operations and a connection provider will be used to select a connection for blocking and tx operations only, which should not share a connection. If native connection sharing is disabled, new (or pooled) connections will be used for all operations. By default, multiple connections share a single thread-safe native connection. If you enable connection pooling, then native connection sharing will be disabled and the connection pool will be used for all operations. You may however explicitly control connection sharing via this setting as an override.
- `cas.authn.attribute-repository.redis[0].start-tls` (Optional; type `boolean`): Start mutual TLS. In order to support TLS, Redis should be configured with a X.509 certificate and a private key. In addition, it is necessary to specify a CA certificate bundle file or path to be used as a trusted root when validating certificates.
- `cas.authn.attribute-repository.redis[0].timeout` (Optional; type `String`; default `PT60S`): Command timeout.
- `cas.authn.attribute-repository.redis[0].uri` (Optional; type `String`): Database URI.
- `cas.authn.attribute-repository.redis[0].use-ssl` (Optional; type `boolean`): Whether or not to use SSL for connection factory.
- `cas.authn.attribute-repository.redis[0].verify-peer` (Optional; type `boolean`; default `true`): Control how peer verification is handled with redis connections. Peer verification is a security feature that checks if the host you're connecting to is who it says it is. This is often done by checking a digital certificate.
