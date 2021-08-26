
package org.apereo.cas;

import org.apereo.cas.audit.spi.ServiceAccessEnforcementAuditResourceResolverTests;
import org.apereo.cas.audit.spi.ShortenedReturnValueAsStringAuditResourceResolverTests;
import org.apereo.cas.audit.spi.ThreadLocalAuditPrincipalResolverTests;
import org.apereo.cas.audit.spi.TicketValidationResourceResolverTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    ShortenedReturnValueAsStringAuditResourceResolverTests.class,
    ThreadLocalAuditPrincipalResolverTests.class,
    ServiceAccessEnforcementAuditResourceResolverTests.class,
    TicketValidationResourceResolverTests.class
})
@Suite
public class AllTestsSuite {
}
