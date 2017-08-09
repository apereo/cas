package org.apereo.cas;

import org.apereo.cas.authentication.LdapAuthenticationHandlerTests;
import org.apereo.cas.authentication.principal.PersonDirectoryPrincipalResolverLdaptiveTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite to run all LDAP tests.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({LdapAuthenticationHandlerTests.class, 
        PersonDirectoryPrincipalResolverLdaptiveTests.class})
public class AllLdapTestsSuite {
}
