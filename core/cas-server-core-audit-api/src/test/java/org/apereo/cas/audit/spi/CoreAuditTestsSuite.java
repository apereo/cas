package org.apereo.cas.audit.spi;

import org.apereo.cas.audit.spi.principal.ChainingAuditPrincipalIdProviderTests;
import org.apereo.cas.audit.spi.resource.MessageBundleAwareResourceResolverTests;
import org.apereo.cas.audit.spi.resource.ServiceResourceResolverTests;
import org.apereo.cas.audit.spi.resource.TicketAsFirstParameterResourceResolverTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link CoreAuditTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SelectClasses({
    AuditActionContextJsonSerializerTests.class,
    ServiceResourceResolverTests.class,
    TicketAsFirstParameterResourceResolverTests.class,
    ChainingAuditPrincipalIdProviderTests.class,
    FilterAndDelegateAuditTrailManagerTests.class,
    MessageBundleAwareResourceResolverTests.class
})
@RunWith(JUnitPlatform.class)
public class CoreAuditTestsSuite {
}
