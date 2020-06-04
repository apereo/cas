package org.apereo.cas;

import org.apereo.cas.adaptors.redis.services.RedisServerServiceRegistryTests;
import org.apereo.cas.adaptors.redis.services.SentinelServerServiceRegistryTests;

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
    SentinelServerServiceRegistryTests.class
})
@RunWith(JUnitPlatform.class)
public class AllRedisServiceRegistryTestsSuite {
}
