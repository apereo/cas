package org.apereo.cas;

import org.apereo.cas.web.support.MongoDbThrottledSubmissionHandlerInterceptorAdapterTests;

import org.junit.platform.suite.api.SelectClasses;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@SelectClasses(
    MongoDbThrottledSubmissionHandlerInterceptorAdapterTests.class
)
public class AllTestsSuite {
}
