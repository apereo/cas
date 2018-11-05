package org.apereo.cas.audit.spi;

import org.apereo.cas.audit.spi.principal.ChainingAuditPrincipalIdProviderTests;
import org.apereo.cas.audit.spi.resource.ServiceResourceResolverTests;
import org.apereo.cas.audit.spi.resource.TicketAsFirstParameterResourceResolverTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link CoreAuditTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    AuditActionContextJsonSerializerTests.class,
    ServiceResourceResolverTests.class,
    TicketAsFirstParameterResourceResolverTests.class,
    ChainingAuditPrincipalIdProviderTests.class
})
public class CoreAuditTestsSuite {
}
