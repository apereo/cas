package org.apereo.cas;

import org.apereo.cas.web.report.AuditLogEndpointTests;
import org.apereo.cas.web.report.CasInfoEndpointContributorTests;
import org.apereo.cas.web.report.CasReleaseAttributesReportEndpointTests;
import org.apereo.cas.web.report.CasResolveAttributesReportEndpointTests;
import org.apereo.cas.web.report.CasRuntimeModulesEndpointTests;
import org.apereo.cas.web.report.LoggingConfigurationEndpointTests;
import org.apereo.cas.web.report.RegisteredAuthenticationHandlersEndpointTests;
import org.apereo.cas.web.report.RegisteredAuthenticationPoliciesEndpointTests;
import org.apereo.cas.web.report.RegisteredServicesEndpointTests;
import org.apereo.cas.web.report.SingleSignOnSessionStatusEndpointTests;
import org.apereo.cas.web.report.SingleSignOnSessionsEndpointTests;
import org.apereo.cas.web.report.SpringWebflowEndpointTests;
import org.apereo.cas.web.report.StatisticsEndpointTests;
import org.apereo.cas.web.report.StatusEndpointTests;
import org.apereo.cas.web.report.StatusEndpointWithHealthTests;
import org.apereo.cas.web.report.TicketExpirationPoliciesEndpointTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllCasReportsTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SelectClasses({
    AuditLogEndpointTests.class,
    SingleSignOnSessionStatusEndpointTests.class,
    RegisteredServicesEndpointTests.class,
    RegisteredAuthenticationHandlersEndpointTests.class,
    StatusEndpointTests.class,
    StatusEndpointWithHealthTests.class,
    CasRuntimeModulesEndpointTests.class,
    TicketExpirationPoliciesEndpointTests.class,
    StatisticsEndpointTests.class,
    SingleSignOnSessionsEndpointTests.class,
    SpringWebflowEndpointTests.class,
    LoggingConfigurationEndpointTests.class,
    CasInfoEndpointContributorTests.class,
    RegisteredAuthenticationPoliciesEndpointTests.class,
    CasResolveAttributesReportEndpointTests.class,
    CasReleaseAttributesReportEndpointTests.class
})
@Suite
public class AllCasReportsTestsSuite {
}
