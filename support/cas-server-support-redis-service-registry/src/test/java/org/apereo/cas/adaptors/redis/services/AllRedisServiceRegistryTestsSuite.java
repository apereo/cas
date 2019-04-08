package org.apereo.cas.adaptors.redis.services;

import org.junit.platform.suite.api.SelectClasses;

/**
 * This is {@link AllRedisServiceRegistryTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SelectClasses({
    RedisEmbeddedServiceRegistryTests.class,
    SentinelEmbeddedServiceRegistryTests.class,
    RedisServerServiceRegistryTests.class,
    SentinelServerServiceRegistryTests.class
})
public class AllRedisServiceRegistryTestsSuite {
}
