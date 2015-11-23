package org.jasig.cas;

import org.jasig.cas.authentication.LdapAuthenticationHandlerTests;
import org.jasig.cas.authorization.generator.LdapAuthorizationGeneratorTests;
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
    LdapAuthorizationGeneratorTests.class
})
public class AllTestsSuite {
}
