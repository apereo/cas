package org.apereo.cas;

import org.apereo.cas.redis.RedisAuthenticationHandlerTests;
import org.apereo.cas.redis.RedisPersonAttributeDaoTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllRedisAuthenticationTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SelectClasses({
    RedisAuthenticationHandlerTests.class,
    RedisPersonAttributeDaoTests.class
})
@Suite
public class AllRedisAuthenticationTestsSuite {
}
