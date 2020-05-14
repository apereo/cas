package org.apereo.cas;

import org.apereo.cas.authentication.ActiveDirectoryLdapAuthenticationHandlerPasswordPolicyTests;
import org.apereo.cas.authentication.ActiveDirectorySamAccountNameLdapAuthenticationHandlerTests;
import org.apereo.cas.authentication.ActiveDirectoryUPNLdapAuthenticationHandlerTests;
import org.apereo.cas.authentication.AuthenticatedLdapAuthenticationHandlerTests;
import org.apereo.cas.authentication.CustomPasswordPolicyLdapAuthenticationHandlerTests;
import org.apereo.cas.authentication.DirectLdapAuthenticationHandlerTests;
import org.apereo.cas.authentication.EDirectoryPasswordPolicyLdapAuthenticationHandlerTests;
import org.apereo.cas.authentication.FreeIPAPasswordPolicyLdapAuthenticationHandlerTests;
import org.apereo.cas.authentication.LdapPasswordSynchronizationAuthenticationPostProcessorTests;
import org.apereo.cas.authentication.principal.PersonDirectoryPrincipalResolverActiveDirectoryTests;
import org.apereo.cas.authentication.principal.PersonDirectoryPrincipalResolverLdapTests;
import org.apereo.cas.config.LdapPasswordSynchronizationConfigurationTests;

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
    ActiveDirectoryUPNLdapAuthenticationHandlerTests.class,
    ActiveDirectorySamAccountNameLdapAuthenticationHandlerTests.class,
    ActiveDirectoryLdapAuthenticationHandlerPasswordPolicyTests.class,
    AuthenticatedLdapAuthenticationHandlerTests.class,
    PersonDirectoryPrincipalResolverLdapTests.class,
    DirectLdapAuthenticationHandlerTests.class,
    PersonDirectoryPrincipalResolverActiveDirectoryTests.class,
    LdapPasswordSynchronizationConfigurationTests.class,
    EDirectoryPasswordPolicyLdapAuthenticationHandlerTests.class,
    FreeIPAPasswordPolicyLdapAuthenticationHandlerTests.class,
    CustomPasswordPolicyLdapAuthenticationHandlerTests.class,
    LdapPasswordSynchronizationAuthenticationPostProcessorTests.class
})
@RunWith(JUnitPlatform.class)
public class AllLdapTestsSuite {
}
