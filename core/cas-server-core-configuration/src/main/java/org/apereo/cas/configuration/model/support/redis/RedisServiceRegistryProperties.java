package org.apereo.cas.configuration.model.support.redis;

import org.apereo.cas.configuration.support.RequiresModule;

/**
 * Configuration properties for Redis.
 *
 * @author serv
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-redis-service-registry")
public class RedisServiceRegistryProperties extends BaseRedisProperties {
    private static final long serialVersionUID = -9012996050439638782L;
}
