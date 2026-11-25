# Server-side Sessions - Redis

Source: https://apereo.github.io/cas/development/webflow/Webflow-Customization-Sessions-ServerSide-Redis.html

If you don’t wish to use the native container’s strategy for session replication, you can use CAS’s support for Redis session replication.

This feature is enabled via the following module:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-session-redis"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `spring.data.redis.azure.credential` (Third party; type `TokenCredentialProperties`)
- `spring.data.redis.azure.passwordless-enabled` (Third party; type `Boolean`; default `false`): Whether to enable supporting azure identity token credentials, by default is false. If the passwordlessEnabled is true, but the redis password properties is not null, it will still use username/password to authenticate connections.
- `spring.data.redis.azure.profile` (Third party; type `AzureProfileProperties`)
- `spring.data.redis.azure.scopes` (Third party; type `String`)
- `spring.data.redis.client-name` (Third party; type `String`): Client name to be set on connections with CLIENT SETNAME.
- `spring.data.redis.client-type` (Third party; type `DataRedisProperties.ClientType`): Type of client to use. By default, auto-detected according to the classpath.
- `spring.data.redis.cluster.max-redirects` (Third party; type `Integer`): Maximum number of redirects to follow when executing commands across the cluster.
- `spring.data.redis.cluster.nodes` (Third party; type `List<String>`): List of "host:port" pairs to bootstrap from. This represents an "initial" list of cluster nodes and is required to have at least one entry.
- `spring.data.redis.connect-timeout` (Third party; type `Duration`): Connection timeout.
- `spring.data.redis.database` (Third party; type `Integer`; default `0`): Database index used by the connection factory.
- `spring.data.redis.host` (Third party; type `String`; default `localhost`): Redis server host.
- `spring.data.redis.jedis.pool.enabled` (Third party; type `Boolean`): Whether to enable the pool. Enabled automatically if "commons-pool2" is available. With Jedis, pooling is implicitly enabled in sentinel mode and this setting only applies to single node setup.
- `spring.data.redis.jedis.pool.max-active` (Third party; type `Integer`; default `8`): Maximum number of connections that can be allocated by the pool at a given time. Use a negative value for no limit.
- `spring.data.redis.jedis.pool.max-idle` (Third party; type `Integer`; default `8`): Maximum number of "idle" connections in the pool. Use a negative value to indicate an unlimited number of idle connections.
- `spring.data.redis.jedis.pool.max-wait` (Third party; type `Duration`; default `-1ms`): Maximum amount of time a connection allocation should block before throwing an exception when the pool is exhausted. Use a negative value to block indefinitely.
- `spring.data.redis.jedis.pool.min-idle` (Third party; type `Integer`; default `0`): Target for the minimum number of idle connections to maintain in the pool. This setting only has an effect if both it and time between eviction runs are positive.
- `spring.data.redis.jedis.pool.time-between-eviction-runs` (Third party; type `Duration`): Time between runs of the idle object evictor thread. When positive, the idle object evictor thread starts, otherwise no idle object eviction is performed.
- `spring.data.redis.lettuce.cluster.refresh.dynamic-refresh-sources` (Third party; type `Boolean`; default `true`): Whether to discover and query all cluster nodes for obtaining the cluster topology. When set to false, only the initial seed nodes are used as sources for topology discovery.
- `spring.data.redis.lettuce.cluster.refresh.period` (Third party; type `Duration`): Cluster topology refresh period.
- `spring.data.redis.lettuce.pool.enabled` (Third party; type `Boolean`): Whether to enable the pool. Enabled automatically if "commons-pool2" is available. With Jedis, pooling is implicitly enabled in sentinel mode and this setting only applies to single node setup.
- `spring.data.redis.lettuce.pool.max-active` (Third party; type `Integer`; default `8`): Maximum number of connections that can be allocated by the pool at a given time. Use a negative value for no limit.
- `spring.data.redis.lettuce.pool.max-idle` (Third party; type `Integer`; default `8`): Maximum number of "idle" connections in the pool. Use a negative value to indicate an unlimited number of idle connections.
- `spring.data.redis.lettuce.pool.max-wait` (Third party; type `Duration`; default `-1ms`): Maximum amount of time a connection allocation should block before throwing an exception when the pool is exhausted. Use a negative value to block indefinitely.
- `spring.data.redis.lettuce.pool.min-idle` (Third party; type `Integer`; default `0`): Target for the minimum number of idle connections to maintain in the pool. This setting only has an effect if both it and time between eviction runs are positive.
- `spring.data.redis.lettuce.pool.time-between-eviction-runs` (Third party; type `Duration`): Time between runs of the idle object evictor thread. When positive, the idle object evictor thread starts, otherwise no idle object eviction is performed.
- `spring.data.redis.lettuce.read-from` (Third party; type `String`): Defines from which Redis nodes data is read.
- `spring.data.redis.lettuce.shutdown-timeout` (Third party; type `Duration`; default `100ms`): Shutdown timeout.
- `spring.data.redis.listener.auto-startup` (Third party; type `Boolean`; default `true`): Whether to start the container automatically on startup.
- `spring.data.redis.listener.recovery.delay` (Third party; type `Duration`; default `5s`): Base delay for a recovery attempt. Can be combined with a "multiplier" to use an exponential back off strategy.
- `spring.data.redis.listener.recovery.jitter` (Third party; type `Duration`; default `0`): Jitter value for the base retry attempt, randomly subtracted or added to the calculated delay, resulting in a value between 'delay - jitter' and 'delay + jitter' but never below the base delay or above the max delay.
- `spring.data.redis.listener.recovery.max-delay` (Third party; type `Duration`; default `30s`): Maximum delay for any retry attempt, limiting how far jitter and the multiplier can increase the delay.
- `spring.data.redis.listener.recovery.max-retries` (Third party; type `Long`): Maximum number of recovery attempts.
- `spring.data.redis.listener.recovery.multiplier` (Third party; type `Double`; default `1`): Multiplier for a delay for the next retry attempt, applied to the previous delay, starting with the initial delay as well as to the applicable jitter for each attempt. Fixed delay by default.
- `spring.data.redis.listener.subscription-registration-timeout` (Third party; type `Duration`; default `2s`): Maximum amount of time to wait for the subscription to become active.
- `spring.data.redis.masterreplica.nodes` (Third party; type `List<String>`): Static list of "host:port" pairs to use, at least one entry is required.
- `spring.data.redis.password` (Third party; type `String`): Login password of the redis server.
- `spring.data.redis.port` (Third party; type `Integer`; default `6379`): Redis server port.
- `spring.data.redis.repositories.enabled` (Third party; type `Boolean`; default `true`): Whether to enable Redis repositories.
- `spring.data.redis.sentinel.master` (Third party; type `String`): Name of the Redis server.
- `spring.data.redis.sentinel.nodes` (Third party; type `List<String>`): List of "host:port" pairs.
- `spring.data.redis.sentinel.password` (Third party; type `String`): Password for authenticating with sentinel(s).
- `spring.data.redis.sentinel.username` (Third party; type `String`): Login username for authenticating with sentinel(s).
- `spring.data.redis.ssl.bundle` (Third party; type `String`): SSL bundle name.
- `spring.data.redis.ssl.enabled` (Third party; type `Boolean`): Whether to enable SSL support. Enabled automatically if "bundle" is provided unless specified otherwise.
- `spring.data.redis.ssl.verify-hostname` (Third party; type `Boolean`; default `true`): Whether to perform hostname verification.
- `spring.data.redis.timeout` (Third party; type `Duration`): Read timeout.
- `spring.data.redis.url` (Third party; type `String`): Connection URL. Overrides host, port, username, password, and database. Example: redis://user:password@example.com:6379/8
- `spring.data.redis.username` (Third party; type `String`): Login username of the redis server.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.
