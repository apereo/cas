package org.apereo.cas.adaptors.redis;

import org.apereo.cas.adaptors.redis.services.RedisEmbeddedServiceRegistryTests;
import org.apereo.cas.adaptors.redis.services.RedisServerServiceRegistryTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link AllRedisServiceRegistryTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    RedisEmbeddedServiceRegistryTests.class,
    RedisServerServiceRegistryTests.class
})
public class AllRedisServiceRegistryTestsSuite {
}
