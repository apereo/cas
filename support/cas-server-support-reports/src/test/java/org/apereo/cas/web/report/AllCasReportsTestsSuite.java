package org.apereo.cas.web.report;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllCasReportsTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SelectClasses({
    AuditLogEndpointTests.class,
    CasInfoEndpointContributorTests.class
})
@RunWith(JUnitPlatform.class)
public class AllCasReportsTestsSuite {
}
