package org.apereo.cas;

import org.apereo.cas.support.saml.authentication.SamlAuthenticationRequestTests;
import org.apereo.cas.support.saml.authentication.principal.SamlServiceTests;
import org.apereo.cas.support.saml.web.view.Saml10FailureResponseViewTests;
import org.apereo.cas.support.saml.web.view.Saml10SuccessResponseViewTests;
import org.apereo.cas.support.saml.authentication.SamlAuthenticationMetaDataPopulatorTests;
import org.apereo.cas.support.saml.util.SamlCompliantUniqueTicketIdGeneratorTests;
import org.apereo.cas.support.saml.web.support.WebUtilTests;
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
public class AllTestsSuite {
}
