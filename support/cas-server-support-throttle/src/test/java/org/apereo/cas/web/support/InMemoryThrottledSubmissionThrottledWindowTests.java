package org.apereo.cas.web.support;

import org.apereo.cas.authentication.AcceptUsersAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import lombok.Getter;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ThrottledSubmissionHandlerInterceptor}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@EnableScheduling
@SpringBootTest(classes = {
    InMemoryThrottledSubmissionThrottledWindowTests.AuthenticationTestConfiguration.class,
    BaseThrottledSubmissionHandlerInterceptorAdapterTests.SharedTestConfiguration.class
},
    properties = {
        "cas.authn.throttle.failure.range-seconds=5",
        "cas.authn.throttle.failure.threshold=1",
        "cas.authn.throttle.failure.throttle-window-seconds=PT2S"
    }
)
@Getter
@Tag("AuthenticationThrottling")
@ExtendWith(CasTestExtension.class)
class InMemoryThrottledSubmissionThrottledWindowTests
    extends BaseThrottledSubmissionHandlerInterceptorAdapterTests {

    @Autowired
    @Qualifier(ThrottledSubmissionHandlerInterceptor.BEAN_NAME)
    private ThrottledSubmissionHandlerInterceptor throttle;

    @Override
    @Test
    void verifyThrottle() throws Throwable {
        var success = login("casuser", "Mellon", IP_ADDRESS);
        assertEquals(HttpStatus.SC_OK, success.getStatus());

        var result = login("casuser", "bad", IP_ADDRESS);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, result.getStatus());

        result = login("casuser", "bad", IP_ADDRESS);
        assertEquals(HttpStatus.SC_LOCKED, result.getStatus());

        result = login("casuser", "Mellon", IP_ADDRESS);
        assertEquals(HttpStatus.SC_LOCKED, result.getStatus());

        Thread.sleep(5000);

        result = login("casuser", "Mellon", IP_ADDRESS);
        assertEquals(HttpStatus.SC_OK, result.getStatus());
    }

    @TestConfiguration(value = "AuthenticationTestConfiguration", proxyBeanMethods = false)
    static class AuthenticationTestConfiguration {
        @Bean
        public AuthenticationEventExecutionPlanConfigurer surrogateAuthenticationEventExecutionPlanConfigurer() {
            return plan -> plan.registerAuthenticationHandler(new AcceptUsersAuthenticationHandler(CollectionUtils.wrap("casuser", "Mellon")));
        }
    }
}
