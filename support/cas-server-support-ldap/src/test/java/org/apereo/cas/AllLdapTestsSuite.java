package org.apereo.cas;

import org.apereo.cas.authentication.ActiveDirectoryJndiSamAccountNameLdapAuthenticationHandlerTests;
import org.apereo.cas.authentication.ActiveDirectoryJndiUPNLdapAuthenticationHandlerTests;
import org.apereo.cas.authentication.ActiveDirectoryUnboundIDBindDnSSLLdapAuthenticationHandlerTests;
import org.apereo.cas.authentication.ActiveDirectoryUnboundIDTypeADAuthenticationHandlerTests;
import org.apereo.cas.authentication.AuthenticatedLdapAuthenticationHandlerTests;
import org.apereo.cas.authentication.DirectLdapAuthenticationHandlerTests;
import org.apereo.cas.authentication.LdapPasswordSynchronizationAuthenticationPostProcessorTests;
import org.apereo.cas.authentication.principal.PersonDirectoryPrincipalResolverLdaptiveTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * Test suite to run all LDAP tests.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@SelectClasses({
    ActiveDirectoryUnboundIDBindDnSSLLdapAuthenticationHandlerTests.class,
    ActiveDirectoryUnboundIDTypeADAuthenticationHandlerTests.class,
    ActiveDirectoryJndiUPNLdapAuthenticationHandlerTests.class,
    ActiveDirectoryJndiSamAccountNameLdapAuthenticationHandlerTests.class,
    AuthenticatedLdapAuthenticationHandlerTests.class,
    PersonDirectoryPrincipalResolverLdaptiveTests.class,
    DirectLdapAuthenticationHandlerTests.class,
    LdapPasswordSynchronizationAuthenticationPostProcessorTests.class
})
@RunWith(JUnitPlatform.class)
public class AllLdapTestsSuite {
}
