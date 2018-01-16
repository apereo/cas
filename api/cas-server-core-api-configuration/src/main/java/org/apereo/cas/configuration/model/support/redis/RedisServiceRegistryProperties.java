package org.apereo.cas.configuration.model.support.redis;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;

/**
 * Configuration properties for Redis.
 *
 * @author serv
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-redis-service-registry")
@Slf4j
@Getter
@Setter
public class RedisServiceRegistryProperties extends BaseRedisProperties {

    private static final long serialVersionUID = -9012996050439638782L;
}
