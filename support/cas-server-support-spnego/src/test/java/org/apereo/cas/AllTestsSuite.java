package org.apereo.cas;


import org.apereo.cas.support.spnego.authentication.handler.support.JcifsSpnegoAuthenticationHandlerTests;
import org.apereo.cas.support.spnego.authentication.principal.SpnegoCredentialsTests;
import org.apereo.cas.support.spnego.authentication.principal.SpnegoCredentialsToPrincipalResolverTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite to run all LDAP tests.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        JcifsSpnegoAuthenticationHandlerTests.class,
        SpnegoCredentialsTests.class,
        SpnegoCredentialsToPrincipalResolverTests.class
})
public class AllTestsSuite {
}
