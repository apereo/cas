package org.apereo.cas.support.saml.web.view;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationAttributeReleasePolicy;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.RememberMeCredential;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.support.DefaultCasProtocolAttributeEncoder;
import org.apereo.cas.services.DefaultServicesManager;
import org.apereo.cas.services.InMemoryServiceRegistry;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.saml.AbstractOpenSamlTests;
import org.apereo.cas.support.saml.authentication.SamlAuthenticationMetaDataPopulator;
import org.apereo.cas.support.saml.authentication.principal.SamlServiceFactory;
import org.apereo.cas.support.saml.util.Saml10ObjectBuilder;
import org.apereo.cas.validation.DefaultAssertionBuilder;
import org.apereo.cas.web.support.DefaultArgumentExtractor;
import org.apereo.cas.web.view.attributes.NoOpProtocolAttributesRenderer;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link Saml10SuccessResponseView} class.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.1
 */
public class Saml10SuccessResponseViewTests extends AbstractOpenSamlTests {

    private static final String TEST_VALUE = "testValue";
    private static final String TEST_ATTRIBUTE = "testAttribute";
    private static final String PRINCIPAL_ID = "testPrincipal";

    private Saml10SuccessResponseView response;

    @BeforeEach
    public void initialize() {
        val list = new ArrayList<RegisteredService>();
        list.add(RegisteredServiceTestUtils.getRegisteredService("https://.+"));
        val dao = new InMemoryServiceRegistry(mock(ApplicationEventPublisher.class), list);

        val mgmr = new DefaultServicesManager(dao, mock(ApplicationEventPublisher.class), new HashSet<>());
        mgmr.load();

        this.response = new Saml10SuccessResponseView(new DefaultCasProtocolAttributeEncoder(mgmr, CipherExecutor.noOpOfStringToString()),
            mgmr,
            new Saml10ObjectBuilder(configBean),
            new DefaultArgumentExtractor(new SamlServiceFactory()),
            StandardCharsets.UTF_8.name(), 1000, 30,
            "testIssuer",
            "whatever",
            new DefaultAuthenticationAttributeReleasePolicy("attribute"),
            new DefaultAuthenticationServiceSelectionPlan(),
            new NoOpProtocolAttributesRenderer());
    }

    @Test
    public void verifyResponse() throws Exception {
        val model = new HashMap<String, Object>();

        val attributes = new HashMap<String, Object>();
        attributes.put(TEST_ATTRIBUTE, TEST_VALUE);
        attributes.put("testEmptyCollection", new ArrayList<>(0));
        attributes.put("testAttributeCollection", Arrays.asList("tac1", "tac2"));
        val principal = new DefaultPrincipalFactory().createPrincipal(PRINCIPAL_ID, attributes);

        val authAttributes = new HashMap<String, Object>();
        authAttributes.put(
            SamlAuthenticationMetaDataPopulator.ATTRIBUTE_AUTHENTICATION_METHOD,
            SamlAuthenticationMetaDataPopulator.AUTHN_METHOD_SSL_TLS_CLIENT);
        authAttributes.put("testSamlAttribute", "value");

        val primary = CoreAuthenticationTestUtils.getAuthentication(principal, authAttributes);
        val assertion = new DefaultAssertionBuilder(primary).with(Collections.singletonList(primary)).with(
            CoreAuthenticationTestUtils.getService()).with(true).build();
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
    public void verifyResponseWithNoAttributes() throws Exception {
        val model = new HashMap<String, Object>();

        val principal = new DefaultPrincipalFactory().createPrincipal(PRINCIPAL_ID);

        val authAttributes = new HashMap<String, Object>();
        authAttributes.put(
            SamlAuthenticationMetaDataPopulator.ATTRIBUTE_AUTHENTICATION_METHOD,
            SamlAuthenticationMetaDataPopulator.AUTHN_METHOD_SSL_TLS_CLIENT);
        authAttributes.put("testSamlAttribute", "value");

        val primary = CoreAuthenticationTestUtils.getAuthentication(principal, authAttributes);
        val assertion = new DefaultAssertionBuilder(primary)
            .with(Collections.singletonList(primary))
            .with(CoreAuthenticationTestUtils.getService())
            .with(true)
            .build();

        model.put("assertion", assertion);

        val servletResponse = new MockHttpServletResponse();

        this.response.renderMergedOutputModel(model, new MockHttpServletRequest(), servletResponse);
        val written = servletResponse.getContentAsString();

        assertTrue(written.contains(PRINCIPAL_ID));
        assertTrue(written.contains(SamlAuthenticationMetaDataPopulator.AUTHN_METHOD_SSL_TLS_CLIENT));
        assertTrue(written.contains("AuthenticationMethod="));
    }

    @Test
    public void verifyResponseWithoutAuthMethod() throws Exception {
        val model = new HashMap<String, Object>();

        val attributes = new HashMap<String, Object>();
        attributes.put(TEST_ATTRIBUTE, TEST_VALUE);
        val principal = new DefaultPrincipalFactory().createPrincipal(PRINCIPAL_ID, attributes);

        val authnAttributes = new HashMap<String, Object>();
        authnAttributes.put("authnAttribute1", "authnAttrbuteV1");
        authnAttributes.put("authnAttribute2", "authnAttrbuteV2");
        authnAttributes.put(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME, Boolean.TRUE);

        val primary = CoreAuthenticationTestUtils.getAuthentication(principal, authnAttributes);

        val assertion = new DefaultAssertionBuilder(primary)
            .with(Collections.singletonList(primary))
            .with(CoreAuthenticationTestUtils.getService())
            .with(true)
            .build();
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
}
