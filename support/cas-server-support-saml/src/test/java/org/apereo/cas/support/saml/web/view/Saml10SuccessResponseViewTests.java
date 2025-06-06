package org.apereo.cas.support.saml.web.view;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationAttributeReleasePolicy;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.RememberMeCredential;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.support.DefaultCasProtocolAttributeEncoder;
import org.apereo.cas.authentication.support.NoOpProtocolAttributeEncoder;
import org.apereo.cas.services.RegisteredServicePublicKeyCipherExecutor;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.saml.AbstractOpenSamlTests;
import org.apereo.cas.support.saml.authentication.SamlAuthenticationMetaDataPopulator;
import org.apereo.cas.support.saml.authentication.SamlResponseBuilder;
import org.apereo.cas.support.saml.authentication.principal.SamlServiceFactory;
import org.apereo.cas.support.saml.util.Saml10ObjectBuilder;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.validation.Assertion;
import org.apereo.cas.validation.DefaultAssertionBuilder;
import org.apereo.cas.web.support.DefaultArgumentExtractor;
import org.apereo.cas.web.view.attributes.NoOpProtocolAttributesRenderer;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for {@link Saml10SuccessResponseView} class.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.1
 */
@Tag("SAML1")
class Saml10SuccessResponseViewTests extends AbstractOpenSamlTests {

    private static final String TEST_VALUE = "testValue";

    private static final String TEST_ATTRIBUTE = "testAttribute";

    private static final String PRINCIPAL_ID = "testPrincipal";

    private Saml10SuccessResponseView response;

    @BeforeEach
    void initialize() {
        servicesManager.save(RegisteredServiceTestUtils.getRegisteredService("https://.+"));
        val protocolAttributeEncoder = new DefaultCasProtocolAttributeEncoder(servicesManager,
            RegisteredServicePublicKeyCipherExecutor.INSTANCE, CipherExecutor.noOpOfStringToString());
        val builder = new Saml10ObjectBuilder(configBean);
        val samlResponseBuilder = new SamlResponseBuilder(builder, "testIssuer",
            "whatever", "PT1000S", "PT30S",
            new NoOpProtocolAttributeEncoder(), servicesManager);
        this.response = new Saml10SuccessResponseView(protocolAttributeEncoder,
            servicesManager,
            new DefaultArgumentExtractor(List.of(new SamlServiceFactory(tenantExtractor, urlValidator))),
            new DefaultAuthenticationAttributeReleasePolicy("attribute"),
            new DefaultAuthenticationServiceSelectionPlan(),
            NoOpProtocolAttributesRenderer.INSTANCE,
            samlResponseBuilder, attributeDefinitionStore);
    }

    @Test
    void verifyResponse() throws Throwable {
        val model = new HashMap<String, Object>();

        val attributes = new HashMap<String, List<Object>>();
        attributes.put(TEST_ATTRIBUTE, List.of(TEST_VALUE));
        attributes.put("testEmptyCollection", new ArrayList<>());
        attributes.put("testAttributeCollection", Arrays.asList("tac1", "tac2"));
        val principal = PrincipalFactoryUtils.newPrincipalFactory().createPrincipal(PRINCIPAL_ID, attributes);

        val authAttributes = new HashMap<String, List<Object>>();
        authAttributes.put(
            SamlAuthenticationMetaDataPopulator.ATTRIBUTE_AUTHENTICATION_METHOD,
            List.of(SamlAuthenticationMetaDataPopulator.AUTHN_METHOD_SSL_TLS_CLIENT));
        authAttributes.put("testSamlAttribute", List.of("value"));

        val primary = CoreAuthenticationTestUtils.getAuthentication(principal, authAttributes);
        val assertion = getAssertion(primary);
        model.put("assertion", assertion);

        val servletResponse = new MockHttpServletResponse();

        this.response.renderMergedOutputModel(model, new MockHttpServletRequest(), servletResponse);
        val written = servletResponse.getContentAsString();

        assertTrue(written.contains(PRINCIPAL_ID));
        assertTrue(written.contains(TEST_ATTRIBUTE));
        assertTrue(written.contains(TEST_VALUE));
        assertFalse(written.contains("testEmptyCollection"));
        assertTrue(written.contains("testAttributeCollection"));
        assertTrue(written.contains("tac1"));
        assertTrue(written.contains("tac2"));
        assertTrue(written.contains(SamlAuthenticationMetaDataPopulator.AUTHN_METHOD_SSL_TLS_CLIENT));
        assertTrue(written.contains("AuthenticationMethod"));
        assertTrue(written.contains("AssertionID"));
        assertTrue(written.contains("saml1:Attribute"));
        assertTrue(written.contains("saml1p:Response"));
        assertTrue(written.contains("saml1:Assertion"));
    }

    @Test
    void verifyResponseWithNoAttributes() throws Throwable {
        val model = new HashMap<String, Object>();

        val principal = PrincipalFactoryUtils.newPrincipalFactory().createPrincipal(PRINCIPAL_ID);

        val authAttributes = new HashMap<String, List<Object>>();
        authAttributes.put(
            SamlAuthenticationMetaDataPopulator.ATTRIBUTE_AUTHENTICATION_METHOD,
            List.of(SamlAuthenticationMetaDataPopulator.AUTHN_METHOD_SSL_TLS_CLIENT));
        authAttributes.put("testSamlAttribute", List.of("value"));

        val primary = CoreAuthenticationTestUtils.getAuthentication(principal, authAttributes);
        val assertion = getAssertion(primary);
        model.put("assertion", assertion);

        val servletResponse = new MockHttpServletResponse();

        this.response.renderMergedOutputModel(model, new MockHttpServletRequest(), servletResponse);
        val written = servletResponse.getContentAsString();

        assertTrue(written.contains(PRINCIPAL_ID));
        assertTrue(written.contains(SamlAuthenticationMetaDataPopulator.AUTHN_METHOD_SSL_TLS_CLIENT));
        assertTrue(written.contains("AuthenticationMethod="));
    }

    @Test
    void verifyResponseWithoutAuthMethod() throws Throwable {
        val model = new HashMap<String, Object>();

        val attributes = new HashMap<String, List<Object>>();
        attributes.put(TEST_ATTRIBUTE, List.of(TEST_VALUE));
        val principal = PrincipalFactoryUtils.newPrincipalFactory().createPrincipal(PRINCIPAL_ID, attributes);

        val authnAttributes = new HashMap<String, List<Object>>();
        authnAttributes.put("authnAttribute1", List.of("authnAttrbuteV1"));
        authnAttributes.put("authnAttribute2", List.of("authnAttrbuteV2"));
        authnAttributes.put(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME, List.of(Boolean.TRUE));

        val primary = CoreAuthenticationTestUtils.getAuthentication(principal, authnAttributes);

        val assertion = getAssertion(primary);
        model.put("assertion", assertion);

        val servletResponse = new MockHttpServletResponse();

        this.response.renderMergedOutputModel(model, new MockHttpServletRequest(), servletResponse);
        val written = servletResponse.getContentAsString();

        assertTrue(written.contains(PRINCIPAL_ID));
        assertTrue(written.contains(TEST_ATTRIBUTE));
        assertTrue(written.contains(TEST_VALUE));
        assertTrue(written.contains("authnAttribute1"));
        assertTrue(written.contains("authnAttribute2"));
        assertTrue(written.contains(CasProtocolConstants.VALIDATION_REMEMBER_ME_ATTRIBUTE_NAME));
        assertTrue(written.contains("urn:oasis:names:tc:SAML:1.0:am:unspecified"));
    }

    private static Assertion getAssertion(final Authentication primary) {
        val service = CoreAuthenticationTestUtils.getWebApplicationService();
        return DefaultAssertionBuilder.builder()
            .primaryAuthentication(primary)
            .authentications(List.of(primary))
            .service(service)
            .newLogin(true)
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService(service.getId()))
            .build()
            .assemble();
    }
}
