package org.apereo.cas;

import org.apereo.cas.support.saml.SamlAttributeEncoderTests;
import org.apereo.cas.support.saml.SamlIdPConfigurationTests;
import org.apereo.cas.support.saml.services.GroovySamlRegisteredServiceAttributeReleasePolicyTests;
import org.apereo.cas.support.saml.services.PatternMatchingEntityIdAttributeReleasePolicyTests;
import org.apereo.cas.support.saml.services.SamlRegisteredServiceJpaMicrosoftSqlServerTests;
import org.apereo.cas.support.saml.services.SamlRegisteredServiceJpaPostgresTests;
import org.apereo.cas.support.saml.services.SamlRegisteredServiceJpaTests;
import org.apereo.cas.support.saml.services.SamlRegisteredServiceTests;
import org.apereo.cas.support.saml.services.logout.SamlIdPSingleLogoutServiceLogoutUrlBuilderTests;
import org.apereo.cas.support.saml.services.logout.SamlProfileSingleLogoutMessageCreatorTests;
import org.apereo.cas.support.saml.util.SamlIdPUtilsTests;
import org.apereo.cas.support.saml.web.idp.profile.builders.attr.SamlProfileSamlRegisteredServiceAttributeBuilderTests;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlObjectSignatureValidatorTests;
import org.apereo.cas.support.saml.web.idp.profile.builders.nameid.SamlProfileSamlNameIdBuilderTests;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.SamlProfileSaml2ResponseBuilderTests;

import org.junit.platform.suite.api.SelectClasses;

/**
 * Test suite to run all SAML tests.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@SelectClasses({
    SamlRegisteredServiceTests.class,
    SamlIdPConfigurationTests.class,
    SamlAttributeEncoderTests.class,
    SamlRegisteredServiceJpaTests.class,
    SamlProfileSamlNameIdBuilderTests.class,
    SamlProfileSingleLogoutMessageCreatorTests.class,
    SamlIdPSingleLogoutServiceLogoutUrlBuilderTests.class,
    SamlRegisteredServiceJpaMicrosoftSqlServerTests.class,
    PatternMatchingEntityIdAttributeReleasePolicyTests.class,
    SamlProfileSamlRegisteredServiceAttributeBuilderTests.class,
    GroovySamlRegisteredServiceAttributeReleasePolicyTests.class,
    SamlRegisteredServiceJpaPostgresTests.class,
    SamlIdPUtilsTests.class,
    SamlObjectSignatureValidatorTests.class,
    SamlProfileSaml2ResponseBuilderTests.class
})
public class AllTestsSuite {
}

