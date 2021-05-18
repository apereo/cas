package org.apereo.cas.audit.spi;

import org.apereo.cas.audit.spi.plan.DefaultAuditTrailRecordResolutionPlanTests;
import org.apereo.cas.audit.spi.principal.ChainingAuditPrincipalIdProviderTests;
import org.apereo.cas.audit.spi.resource.CredentialsAsFirstParameterResourceResolverTests;
import org.apereo.cas.audit.spi.resource.MessageBundleAwareResourceResolverTests;
import org.apereo.cas.audit.spi.resource.ServiceAuditResourceResolverTests;
import org.apereo.cas.audit.spi.resource.TicketAsFirstParameterResourceResolverTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link CoreAuditTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SelectClasses({
    AuditActionContextJsonSerializerTests.class,
    ServiceAuditResourceResolverTests.class,
    DefaultAuditTrailRecordResolutionPlanTests.class,
    TicketAsFirstParameterResourceResolverTests.class,
    ChainingAuditPrincipalIdProviderTests.class,
    CredentialsAsFirstParameterResourceResolverTests.class,
    FilterAndDelegateAuditTrailManagerTests.class,
    MessageBundleAwareResourceResolverTests.class
})
@Suite
public class CoreAuditTestsSuite {
}
