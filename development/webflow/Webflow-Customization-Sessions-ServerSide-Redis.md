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

- `spring.redis.client-name` (Third party; type `String`; deprecation `ERROR`, replaced by `spring.data.redis.client-name`)
- `spring.redis.client-type` (Third party; type `DataRedisProperties.ClientType`; deprecation `ERROR`, replaced by `spring.data.redis.client-type`)
- `spring.redis.cluster.max-redirects` (Third party; type `Integer`; deprecation `ERROR`, replaced by `spring.data.redis.cluster.max-redirects`)
- `spring.redis.cluster.nodes` (Third party; type `List<String>`; deprecation `ERROR`, replaced by `spring.data.redis.cluster.nodes`)
- `spring.redis.connect-timeout` (Third party; type `Duration`; deprecation `ERROR`, replaced by `spring.data.redis.connect-timeout`)
- `spring.redis.database` (Third party; type `Integer`; deprecation `ERROR`, replaced by `spring.data.redis.database`)
- `spring.redis.host` (Third party; type `String`; deprecation `ERROR`, replaced by `spring.data.redis.host`)
- `spring.redis.jedis.pool.enabled` (Third party; type `Boolean`; deprecation `ERROR`, no replacement)
- `spring.redis.jedis.pool.max-active` (Third party; type `Integer`; deprecation `ERROR`, no replacement)
- `spring.redis.jedis.pool.max-idle` (Third party; type `Integer`; deprecation `ERROR`, no replacement)
- `spring.redis.jedis.pool.max-wait` (Third party; type `Duration`; deprecation `ERROR`, no replacement)
- `spring.redis.jedis.pool.min-idle` (Third party; type `Integer`; deprecation `ERROR`, no replacement)
- `spring.redis.jedis.pool.time-between-eviction-runs` (Third party; type `Duration`; deprecation `ERROR`, no replacement)
- `spring.redis.lettuce.cluster.refresh.adaptive` (Third party; type `Boolean`; deprecation `ERROR`, replaced by `spring.data.redis.lettuce.cluster.refresh.adaptive`)
- `spring.redis.lettuce.cluster.refresh.dynamic-refresh-sources` (Third party; type `Boolean`; deprecation `ERROR`, replaced by `spring.data.redis.lettuce.cluster.refresh.dynamic-refresh-sources`)
- `spring.redis.lettuce.cluster.refresh.period` (Third party; type `Duration`; deprecation `ERROR`, replaced by `spring.data.redis.lettuce.cluster.refresh.period`)
- `spring.redis.lettuce.pool.enabled` (Third party; type `Boolean`; deprecation `ERROR`, no replacement)
- `spring.redis.lettuce.pool.max-active` (Third party; type `Integer`; deprecation `ERROR`, no replacement)
- `spring.redis.lettuce.pool.max-idle` (Third party; type `Integer`; deprecation `ERROR`, no replacement)
- `spring.redis.lettuce.pool.max-wait` (Third party; type `Duration`; deprecation `ERROR`, no replacement)
- `spring.redis.lettuce.pool.min-idle` (Third party; type `Integer`; deprecation `ERROR`, no replacement)
- `spring.redis.lettuce.pool.time-between-eviction-runs` (Third party; type `Duration`; deprecation `ERROR`, no replacement)
- `spring.redis.lettuce.shutdown-timeout` (Third party; type `Duration`; deprecation `ERROR`, replaced by `spring.data.redis.lettuce.shutdown-timeout`)
- `spring.redis.password` (Third party; type `String`; deprecation `ERROR`, replaced by `spring.data.redis.password`)
- `spring.redis.port` (Third party; type `Integer`; deprecation `ERROR`, replaced by `spring.data.redis.port`)
- `spring.redis.sentinel.master` (Third party; type `String`; deprecation `ERROR`, replaced by `spring.data.redis.sentinel.master`)
- `spring.redis.sentinel.nodes` (Third party; type `List<String>`; deprecation `ERROR`, replaced by `spring.data.redis.sentinel.nodes`)
- `spring.redis.sentinel.password` (Third party; type `String`; deprecation `ERROR`, replaced by `spring.data.redis.sentinel.password`)
- `spring.redis.sentinel.username` (Third party; type `String`; deprecation `ERROR`, replaced by `spring.data.redis.sentinel.username`)
- `spring.redis.ssl` (Third party; type `Boolean`; deprecation `ERROR`, replaced by `spring.data.redis.ssl`)
- `spring.redis.timeout` (Third party; type `Duration`; deprecation `ERROR`, replaced by `spring.data.redis.timeout`)
- `spring.redis.url` (Third party; type `String`; deprecation `ERROR`, replaced by `spring.data.redis.url`)
- `spring.redis.username` (Third party; type `String`; deprecation `ERROR`, replaced by `spring.data.redis.username`)
- `spring.session.redis.cleanup-cron` (Third party; deprecation `ERROR`, replaced by `spring.session.data.redis.cleanup-cron`): Cron expression for expired session cleanup job. Only supported when repository-type is set to indexed. Not supported with a reactive session repository.
- `spring.session.redis.configure-action` (Third party; deprecation `ERROR`, replaced by `spring.session.data.redis.configure-action`): The configure action to apply when no user-defined ConfigureRedisAction or ConfigureReactiveRedisAction bean is present.
- `spring.session.redis.flush-mode` (Third party; deprecation `ERROR`, replaced by `spring.session.data.redis.flush-mode`): Sessions flush mode. Determines when session changes are written to the session store. Not supported with a reactive session repository.
- `spring.session.redis.namespace` (Third party; deprecation `ERROR`, replaced by `spring.session.data.redis.namespace`): Namespace for keys used to store sessions.
- `spring.session.redis.repository-type` (Third party; deprecation `ERROR`, replaced by `spring.session.data.redis.repository-type`): Type of Redis session repository to configure.
- `spring.session.redis.save-mode` (Third party; deprecation `ERROR`, replaced by `spring.session.data.redis.save-mode`): Sessions save mode. Determines how session changes are tracked and saved to the session store.
