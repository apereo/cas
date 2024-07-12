package org.apereo.cas.web.support;

import org.apereo.cas.test.CasTestExtension;
import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Unit test for {@link ThrottledSubmissionHandlerInterceptor}.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@EnableScheduling
@SpringBootTest(classes = BaseThrottledSubmissionHandlerInterceptorAdapterTests.SharedTestConfiguration.class,
    properties = "cas.authn.throttle.failure.range-seconds=5"
)
@Getter
@Tag("AuthenticationThrottling")
@ExtendWith(CasTestExtension.class)
class InMemoryThrottledSubmissionByIpAddressHandlerInterceptorAdapterTests
    extends BaseThrottledSubmissionHandlerInterceptorAdapterTests {

    @Autowired
    @Qualifier(ThrottledSubmissionHandlerInterceptor.BEAN_NAME)
    private ThrottledSubmissionHandlerInterceptor throttle;
}
