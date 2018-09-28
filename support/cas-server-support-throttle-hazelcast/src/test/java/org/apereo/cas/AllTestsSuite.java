package org.apereo.cas;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.web.support.InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapterTests;
import org.apereo.cas.web.support.config.CasHazelcastThrottlingConfiguration;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapterTests.class,
        InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapterTests.class
})
@SpringBootTest(classes = {CasHazelcastThrottlingConfiguration.class})
@Slf4j
public class AllTestsSuite {
}
