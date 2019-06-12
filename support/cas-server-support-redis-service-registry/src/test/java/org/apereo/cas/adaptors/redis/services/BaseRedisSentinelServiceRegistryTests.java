package org.apereo.cas.adaptors.redis.services;

import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.RedisServiceRegistryConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.AbstractServiceRegistryTests;
import org.apereo.cas.services.ServiceRegistry;

import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Common class of Unit test for {@link RedisServiceRegistry} class.
 *
 * @author Julien Gribonvald
 * @since 6.1.0
 */
@SpringBootTest(classes = {
    RedisServiceRegistryConfiguration.class,
    CasCoreServicesConfiguration.class,
    RefreshAutoConfiguration.class
})
@EnableScheduling
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("Redis")
public abstract class BaseRedisSentinelServiceRegistryTests extends AbstractServiceRegistryTests {
    @Autowired
    @Qualifier("redisServiceRegistry")
    private ServiceRegistry dao;

    @Override
    public ServiceRegistry getNewServiceRegistry() {
        return this.dao;
    }
}
