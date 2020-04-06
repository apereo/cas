package org.apereo.cas;

import org.apereo.cas.web.support.JdbcThrottledSubmissionHandlerInterceptorAdapterTests;
import org.apereo.cas.web.support.MySQLJdbcThrottledSubmissionHandlerInterceptorAdapterTests;
import org.apereo.cas.web.support.PostgresJdbcThrottledSubmissionHandlerInterceptorAdapterTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link JdbcThrottledAllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SelectClasses({
    JdbcThrottledSubmissionHandlerInterceptorAdapterTests.class,
    PostgresJdbcThrottledSubmissionHandlerInterceptorAdapterTests.class,
    MySQLJdbcThrottledSubmissionHandlerInterceptorAdapterTests.class
})
@RunWith(JUnitPlatform.class)
public class JdbcThrottledAllTestsSuite {
}
