package org.apereo.cas.web.support;

import org.apereo.cas.config.CasRedisThrottlingConfiguration;
import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This is  {@link RedisThrottledSubmissionHandlerInterceptorAdapterTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Tag("Redis")
@SpringBootTest(classes = {
    CasRedisThrottlingConfiguration.class,
    BaseThrottledSubmissionHandlerInterceptorAdapterTests.SharedTestConfiguration.class
},
    properties = {
        "cas.authn.throttle.usernameParameter=username",
        "cas.audit.redis.host=localhost",
        "cas.audit.redis.port=6379"
    })
@Getter
@EnabledIfContinuousIntegration
@EnabledIfPortOpen(port = 6379)
public class RedisThrottledSubmissionHandlerInterceptorAdapterTests extends BaseThrottledSubmissionHandlerInterceptorAdapterTests {

    @Autowired
    @Qualifier("authenticationThrottle")
    private ThrottledSubmissionHandlerInterceptor throttle;

}
