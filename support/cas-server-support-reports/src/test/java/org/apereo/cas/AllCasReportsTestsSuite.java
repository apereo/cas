package org.apereo.cas;

import org.apereo.cas.web.report.AuditLogEndpointTests;
import org.apereo.cas.web.report.CasInfoEndpointContributorTests;
import org.apereo.cas.web.report.CasReleaseAttributesReportEndpointTests;
import org.apereo.cas.web.report.CasResolveAttributesReportEndpointTests;
import org.apereo.cas.web.report.ExportRegisteredServicesEndpointTests;
import org.apereo.cas.web.report.LoggingConfigurationEndpointTests;
import org.apereo.cas.web.report.RegisteredServicesEndpointTests;
import org.apereo.cas.web.report.SingleSignOnSessionsEndpointTests;
import org.apereo.cas.web.report.SpringWebflowEndpointTests;
import org.apereo.cas.web.report.StatisticsEndpointTests;
import org.apereo.cas.web.report.StatusEndpointTests;
import org.apereo.cas.web.report.StatusEndpointWithHealthTests;

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
    RegisteredServicesEndpointTests.class,
    StatusEndpointTests.class,
    StatusEndpointWithHealthTests.class,
    StatisticsEndpointTests.class,
    SingleSignOnSessionsEndpointTests.class,
    SpringWebflowEndpointTests.class,
    LoggingConfigurationEndpointTests.class,
    CasInfoEndpointContributorTests.class,
    CasResolveAttributesReportEndpointTests.class,
    CasReleaseAttributesReportEndpointTests.class,
    ExportRegisteredServicesEndpointTests.class
})
@RunWith(JUnitPlatform.class)
public class AllCasReportsTestsSuite {
}
