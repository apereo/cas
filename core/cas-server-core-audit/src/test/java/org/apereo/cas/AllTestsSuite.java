
package org.apereo.cas;

import org.apereo.cas.audit.spi.ServiceAccessEnforcementAuditResourceResolverTests;
import org.apereo.cas.audit.spi.ShortenedReturnValueAsStringResourceResolverTests;
import org.apereo.cas.audit.spi.ThreadLocalPrincipalResolverTests;
import org.apereo.cas.audit.spi.TicketValidationResourceResolverTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    ShortenedReturnValueAsStringResourceResolverTests.class,
    ThreadLocalPrincipalResolverTests.class,
    ServiceAccessEnforcementAuditResourceResolverTests.class,
    TicketValidationResourceResolverTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
