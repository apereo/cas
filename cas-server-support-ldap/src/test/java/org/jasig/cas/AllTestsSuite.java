package org.jasig.cas;

import org.jasig.cas.authentication.LdapAuthenticationHandlerTests;
import org.jasig.cas.userdetails.LdapUserDetailsServiceTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite to run all LDAP tests.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    LdapAuthenticationHandlerTests.class,
    LdapUserDetailsServiceTests.class
})
public class AllTestsSuite {
}
