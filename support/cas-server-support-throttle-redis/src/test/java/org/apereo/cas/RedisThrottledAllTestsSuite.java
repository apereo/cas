package org.apereo.cas;

import org.apereo.cas.web.support.RedisSentinelThrottledSubmissionHandlerInterceptorAdapterTests;
import org.apereo.cas.web.support.RedisThrottledSubmissionHandlerInterceptorAdapterTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link RedisThrottledAllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SelectClasses({
    RedisThrottledSubmissionHandlerInterceptorAdapterTests.class,
    RedisSentinelThrottledSubmissionHandlerInterceptorAdapterTests.class
})
@RunWith(JUnitPlatform.class)
public class RedisThrottledAllTestsSuite {
}
