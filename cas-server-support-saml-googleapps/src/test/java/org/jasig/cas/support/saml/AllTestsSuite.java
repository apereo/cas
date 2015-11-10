package org.jasig.cas.support.saml;

import org.jasig.cas.support.saml.authentication.GoogleAppsSamlAuthenticationRequestTests;
import org.jasig.cas.support.saml.authentication.principal.GoogleAccountsServiceFactoryTests;
import org.jasig.cas.support.saml.authentication.principal.GoogleAccountsServiceTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite to run all SAML tests.
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        GoogleAppsSamlAuthenticationRequestTests.class,
        GoogleAccountsServiceTests.class,
        GoogleAccountsServiceFactoryTests.class,
})
public final class AllTestsSuite {
}
