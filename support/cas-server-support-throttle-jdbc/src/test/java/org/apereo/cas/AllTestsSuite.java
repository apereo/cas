package org.apereo.cas;

import org.apereo.cas.web.support.JdbcThrottledSubmissionHandlerInterceptorAdapterTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses(
    JdbcThrottledSubmissionHandlerInterceptorAdapterTests.class
)
public class AllTestsSuite {
}
