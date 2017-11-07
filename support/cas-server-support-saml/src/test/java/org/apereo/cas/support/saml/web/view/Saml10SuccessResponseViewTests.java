package org.apereo.cas.support.saml.web.view;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.RememberMeCredential;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.support.DefaultCasProtocolAttributeEncoder;
import org.apereo.cas.services.DefaultServicesManager;
import org.apereo.cas.services.InMemoryServiceRegistry;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.web.support.DefaultAuthenticationAttributeReleasePolicy;
import org.apereo.cas.support.saml.AbstractOpenSamlTests;
import org.apereo.cas.support.saml.authentication.SamlAuthenticationMetaDataPopulator;
import org.apereo.cas.support.saml.authentication.principal.SamlServiceFactory;
import org.apereo.cas.support.saml.util.Saml10ObjectBuilder;
import org.apereo.cas.util.cipher.NoOpCipherExecutor;
import org.apereo.cas.validation.Assertion;
import org.apereo.cas.validation.DefaultAssertionBuilder;
import org.apereo.cas.web.support.DefaultArgumentExtractor;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Before
    public void setUp() {
        final List<RegisteredService> list = new ArrayList<>();
        list.add(RegisteredServiceTestUtils.getRegisteredService("https://.+"));
        final InMemoryServiceRegistry dao = new InMemoryServiceRegistry();
        dao.setRegisteredServices(list);

        final ServicesManager mgmr = new DefaultServicesManager(dao, mock(ApplicationEventPublisher.class));
        mgmr.load();

        this.response = new Saml10SuccessResponseView(new DefaultCasProtocolAttributeEncoder(mgmr, NoOpCipherExecutor.getInstance()),
                mgmr, "attribute", new Saml10ObjectBuilder(configBean),
                new DefaultArgumentExtractor(new SamlServiceFactory()), StandardCharsets.UTF_8.name(), 1000, 30,
                "testIssuer", "whatever", new DefaultAuthenticationAttributeReleasePolicy());
    }

    @Test
    public void verifyResponse() throws Exception {
        final Map<String, Object> model = new HashMap<>();

        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(TEST_ATTRIBUTE, TEST_VALUE);
        attributes.put("testEmptyCollection", new ArrayList<>(0));
        attributes.put("testAttributeCollection", Arrays.asList("tac1", "tac2"));
        final Principal principal = new DefaultPrincipalFactory().createPrincipal(PRINCIPAL_ID, attributes);

        final Map<String, Object> authAttributes = new HashMap<>();
        authAttributes.put(
                SamlAuthenticationMetaDataPopulator.ATTRIBUTE_AUTHENTICATION_METHOD,
                SamlAuthenticationMetaDataPopulator.AUTHN_METHOD_SSL_TLS_CLIENT);
        authAttributes.put("testSamlAttribute", "value");

        final Authentication primary = CoreAuthenticationTestUtils.getAuthentication(principal, authAttributes);
        final Assertion assertion = new DefaultAssertionBuilder(primary).with(Collections.singletonList(primary)).with(
                CoreAuthenticationTestUtils.getService()).with(true).build();
        model.put("assertion", assertion);

        final MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        this.response.renderMergedOutputModel(model, new MockHttpServletRequest(), servletResponse);
        final String written = servletResponse.getContentAsString();

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
        final Map<String, Object> model = new HashMap<>();

        final Principal principal = new DefaultPrincipalFactory().createPrincipal(PRINCIPAL_ID);

        final Map<String, Object> authAttributes = new HashMap<>();
        authAttributes.put(
                SamlAuthenticationMetaDataPopulator.ATTRIBUTE_AUTHENTICATION_METHOD,
                SamlAuthenticationMetaDataPopulator.AUTHN_METHOD_SSL_TLS_CLIENT);
        authAttributes.put("testSamlAttribute", "value");

        final Authentication primary = CoreAuthenticationTestUtils.getAuthentication(principal, authAttributes);
        final Assertion assertion = new DefaultAssertionBuilder(primary)
                .with(Collections.singletonList(primary))
                .with(CoreAuthenticationTestUtils.getService())
                .with(true)
                .build();

        model.put("assertion", assertion);

        final MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        this.response.renderMergedOutputModel(model, new MockHttpServletRequest(), servletResponse);
        final String written = servletResponse.getContentAsString();

        assertTrue(written.contains(PRINCIPAL_ID));
        assertTrue(written.contains(SamlAuthenticationMetaDataPopulator.AUTHN_METHOD_SSL_TLS_CLIENT));
        assertTrue(written.contains("AuthenticationMethod="));
    }

    @Test
    public void verifyResponseWithoutAuthMethod() throws Exception {
        final Map<String, Object> model = new HashMap<>();

        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(TEST_ATTRIBUTE, TEST_VALUE);
        final Principal principal = new DefaultPrincipalFactory().createPrincipal(PRINCIPAL_ID, attributes);

        final Map<String, Object> authnAttributes = new HashMap<>();
        authnAttributes.put("authnAttribute1", "authnAttrbuteV1");
        authnAttributes.put("authnAttribute2", "authnAttrbuteV2");
        authnAttributes.put(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME, Boolean.TRUE);

        final Authentication primary = CoreAuthenticationTestUtils.getAuthentication(principal, authnAttributes);

        final Assertion assertion = new DefaultAssertionBuilder(primary)
                .with(Collections.singletonList(primary))
                .with(CoreAuthenticationTestUtils.getService())
                .with(true)
                .build();
        model.put("assertion", assertion);

        final MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        this.response.renderMergedOutputModel(model, new MockHttpServletRequest(), servletResponse);
        final String written = servletResponse.getContentAsString();

        assertTrue(written.contains(PRINCIPAL_ID));
        assertTrue(written.contains(TEST_ATTRIBUTE));
        assertTrue(written.contains(TEST_VALUE));
        assertTrue(written.contains("authnAttribute1"));
        assertTrue(written.contains("authnAttribute2"));
        assertTrue(written.contains(CasProtocolConstants.VALIDATION_REMEMBER_ME_ATTRIBUTE_NAME));
        assertTrue(written.contains("urn:oasis:names:tc:SAML:1.0:am:unspecified"));
    }
}
