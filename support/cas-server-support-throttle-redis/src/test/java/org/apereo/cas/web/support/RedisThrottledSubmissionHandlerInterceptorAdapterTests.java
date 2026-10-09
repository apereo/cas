package org.apereo.cas.web.support;

import module java.base;
import org.apereo.cas.config.CasRedisThrottlingAutoConfiguration;
import org.apereo.cas.config.CasSupportRedisAuditAutoConfiguration;
import org.apereo.cas.redis.core.CasRedisTemplate;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

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
        "cas.authn.throttle.failure.range-seconds=3",
        "cas.authn.throttle.failure.threshold=3",
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

    @Autowired
    @Qualifier("throttleRedisTemplate")
    private CasRedisTemplate throttleRedisTemplate;

    @Test
    void verifyFailuresAreBoundedAndExpire() throws Throwable {
        val username = RandomUtils.randomAlphabetic(12);
        for (var i = 0; i < 5; i++) {
            login(username, "badpassword", IP_ADDRESS);
        }
        val key = RedisThrottledSubmissionHandlerInterceptorAdapter.THROTTLED_SUBMISSION_PREFIX
            + IP_ADDRESS + ':' + username.toLowerCase(Locale.ROOT);
        assertEquals(2L, throttleRedisTemplate.opsForZSet().size(key));
        val ttl = throttleRedisTemplate.getExpire(key, TimeUnit.MILLISECONDS);
        assertTrue(ttl > 0 && ttl <= Duration.ofSeconds(3).toMillis());
    }

    @Test
    void verifyUsernameIsCaseInsensitive() throws Throwable {
        val username = RandomUtils.randomAlphabetic(12);
        login(username.toUpperCase(Locale.ROOT), "badpassword", IP_ADDRESS);
        login(username.toLowerCase(Locale.ROOT), "badpassword", IP_ADDRESS);
        val key = RedisThrottledSubmissionHandlerInterceptorAdapter.THROTTLED_SUBMISSION_PREFIX
            + IP_ADDRESS + ':' + username.toLowerCase(Locale.ROOT);
        assertEquals(2L, throttleRedisTemplate.opsForZSet().size(key));
        throttle.clear();
        assertEquals(0L, throttleRedisTemplate.opsForZSet().size(key));
    }
}
