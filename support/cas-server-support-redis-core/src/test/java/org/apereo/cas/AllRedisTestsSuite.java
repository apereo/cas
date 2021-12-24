package org.apereo.cas;

import org.apereo.cas.redis.core.RedisObjectFactoryTests;
import org.apereo.cas.redis.core.util.RedisUtilsTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllRedisTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SelectClasses({
    RedisObjectFactoryTests.class,
    RedisUtilsTests.class
})
@Suite
public class AllRedisTestsSuite {
}
