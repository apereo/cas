package org.apereo.cas;

import org.apereo.cas.adaptors.redis.services.RedisSentinelServerServiceRegistryTests;
import org.apereo.cas.adaptors.redis.services.RedisServerServiceRegistryTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllRedisServiceRegistryTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SelectClasses({
    RedisServerServiceRegistryTests.class,
    RedisSentinelServerServiceRegistryTests.class
})
@RunWith(JUnitPlatform.class)
public class AllRedisServiceRegistryTestsSuite {
}
