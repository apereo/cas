package org.jasig.cas.support.spnego;


import org.jasig.cas.support.spnego.authentication.handler.support.JcifsSpnegoAuthenticationHandlerTests;
import org.jasig.cas.support.spnego.authentication.principal.SpnegoCredentialsTests;
import org.jasig.cas.support.spnego.authentication.principal.SpnegoCredentialsToPrincipalResolverTests;
import org.jasig.cas.support.spnego.web.flow.client.AllSpnegoKnownClientSystemsFilterActionTest;
import org.jasig.cas.support.spnego.web.flow.client.LdapSpnegoKnownClientSystemsFilterActionTests;
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
        SpnegoCredentialsToPrincipalResolverTests.class,
        AllSpnegoKnownClientSystemsFilterActionTest.class,
        LdapSpnegoKnownClientSystemsFilterActionTests.class
})
public class AllTestsSuite {
}
