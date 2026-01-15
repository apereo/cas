package org.apereo.cas.web.support;

import module java.base;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.throttle.DefaultThrottledSubmissionHandlerInterceptorAdapter;
import lombok.Getter;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Unit test for {@link DefaultThrottledSubmissionHandlerInterceptorAdapter}.
 *
 * @author Marvin S. Addison
 * @since 3.0.0
 */

@Tag("AuthenticationThrottling")
@ExtendWith(CasTestExtension.class)
class DefaultThrottledSubmissionHandlerInterceptorAdapterTests {
    @Nested
    @EnableScheduling
    @SpringBootTest(classes = BaseThrottledSubmissionHandlerInterceptorAdapterTests.SharedTestConfiguration.class,
        properties = {
            "cas.authn.throttle.core.username-parameter=username",
            "cas.authn.throttle.core.header-name=User-Agent",
            "cas.authn.throttle.failure.range-seconds=5"
        }
    )
    @Getter
    class WithUsername extends BaseThrottledSubmissionHandlerInterceptorAdapterTests {
        @Autowired
        @Qualifier(ThrottledSubmissionHandlerInterceptor.BEAN_NAME)
        private ThrottledSubmissionHandlerInterceptor throttle;
    }
}
