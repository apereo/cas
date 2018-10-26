package org.apereo.cas;

import org.apereo.cas.web.support.JdbcThrottledSubmissionHandlerInterceptorAdapterTests;

import org.junit.platform.suite.api.SelectClasses;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@SelectClasses(
    JdbcThrottledSubmissionHandlerInterceptorAdapterTests.class
)
public class AllTestsSuite {
}
