package org.jasig.cas.support.saml;

import org.jasig.cas.support.saml.authentication.SamlAuthenticationMetaDataPopulatorTests;
import org.jasig.cas.support.saml.authentication.SamlAuthenticationRequestTests;
import org.jasig.cas.support.saml.authentication.principal.SamlServiceTests;
import org.jasig.cas.support.saml.util.SamlCompliantUniqueTicketIdGeneratorTests;
import org.jasig.cas.support.saml.web.support.WebUtilTests;
import org.jasig.cas.support.saml.web.view.Saml10FailureResponseViewTests;
import org.jasig.cas.support.saml.web.view.Saml10SuccessResponseViewTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite to run all SAML tests.
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        SamlServiceTests.class,
        SamlAuthenticationMetaDataPopulatorTests.class,
        SamlAuthenticationRequestTests.class,
        SamlCompliantUniqueTicketIdGeneratorTests.class,
        WebUtilTests.class,
        Saml10FailureResponseViewTests.class,
        Saml10SuccessResponseViewTests.class
})
public final class AllTestsSuite {
}
