package org.apereo.cas;

import org.apereo.cas.support.saml.InMemoryResourceMetadataResolverTests;
import org.apereo.cas.support.saml.authentication.Saml20ObjectBuilderTests;
import org.apereo.cas.support.saml.authentication.SamlAuthenticationMetaDataPopulatorTests;
import org.apereo.cas.support.saml.authentication.SamlAuthenticationRequestTests;
import org.apereo.cas.support.saml.authentication.principal.SamlServiceFactoryTests;
import org.apereo.cas.support.saml.authentication.principal.SamlServiceTests;
import org.apereo.cas.support.saml.util.SamlCompliantUniqueTicketIdGeneratorTests;
import org.apereo.cas.support.saml.web.SamlValidateEndpointTests;
import org.apereo.cas.support.saml.web.support.WebUtilTests;
import org.apereo.cas.support.saml.web.view.Saml10FailureResponseViewTests;
import org.apereo.cas.support.saml.web.view.Saml10SuccessResponseViewTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Test suite to run all SAML tests.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@SelectClasses({
    SamlServiceTests.class,
    SamlAuthenticationMetaDataPopulatorTests.class,
    SamlAuthenticationRequestTests.class,
    SamlCompliantUniqueTicketIdGeneratorTests.class,
    WebUtilTests.class,
    InMemoryResourceMetadataResolverTests.class,
    SamlValidateEndpointTests.class,
    SamlServiceFactoryTests.class,
    Saml10FailureResponseViewTests.class,
    Saml10SuccessResponseViewTests.class,
    Saml20ObjectBuilderTests.class
})
@Suite
public class AllTestsSuite {
}
