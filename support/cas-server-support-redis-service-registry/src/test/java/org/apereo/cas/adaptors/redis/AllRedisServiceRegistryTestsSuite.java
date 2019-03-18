package org.apereo.cas.adaptors.redis;

import org.apereo.cas.adaptors.redis.services.RedisEmbeddedServiceRegistryTests;
import org.apereo.cas.adaptors.redis.services.RedisServerServiceRegistryTests;
import org.apereo.cas.adaptors.redis.services.SentinelEmbeddedServiceRegistryTests;
import org.apereo.cas.adaptors.redis.services.SentinelServerServiceRegistryTests;

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
