package org.apereo.cas;

import org.apereo.cas.authentication.AuthenticatedLdapAuthenticationHandlerTests;
import org.apereo.cas.authentication.DirectLdapAuthenticationHandlerTests;
import org.apereo.cas.authentication.LdapPasswordSynchronizationAuthenticationPostProcessorTests;
import org.apereo.cas.authentication.principal.PersonDirectoryPrincipalResolverLdaptiveTests;

import org.junit.platform.suite.api.SelectClasses;

/**
 * Test suite to run all LDAP tests.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@SelectClasses({
    AuthenticatedLdapAuthenticationHandlerTests.class,
    PersonDirectoryPrincipalResolverLdaptiveTests.class,
    DirectLdapAuthenticationHandlerTests.class,
    LdapPasswordSynchronizationAuthenticationPostProcessorTests.class
})
public class AllLdapTestsSuite {
}
