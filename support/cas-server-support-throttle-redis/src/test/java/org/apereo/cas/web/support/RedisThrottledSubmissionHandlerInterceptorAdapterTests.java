package org.apereo.cas.web.support;

import module java.base;
import org.apereo.cas.config.CasRedisThrottlingAutoConfiguration;
import org.apereo.cas.config.CasSupportRedisAuditAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
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
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = {
    CasRedisThrottlingAutoConfiguration.class,
    CasSupportRedisAuditAutoConfiguration.class,
    BaseThrottledSubmissionHandlerInterceptorAdapterTests.SharedTestConfiguration.class
},
    properties = {
        "cas.authn.throttle.core.username-parameter=username",
        "cas.authn.throttle.failure.range-seconds=5",
        "cas.audit.redis.host=localhost",
        "cas.audit.redis.port=6379",
        "cas.audit.redis.asynchronous=false"
    })
@Getter
@EnabledIfListeningOnPort(port = 6379)
class RedisThrottledSubmissionHandlerInterceptorAdapterTests extends BaseThrottledSubmissionHandlerInterceptorAdapterTests {

    @Autowired
    @Qualifier(ThrottledSubmissionHandlerInterceptor.BEAN_NAME)
    private ThrottledSubmissionHandlerInterceptor throttle;

}
