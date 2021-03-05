package org.apereo.cas.support.saml.web.view;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationAttributeReleasePolicy;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.RememberMeCredential;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.support.DefaultCasProtocolAttributeEncoder;
import org.apereo.cas.authentication.support.NoOpProtocolAttributeEncoder;
import org.apereo.cas.services.DefaultServicesManager;
import org.apereo.cas.services.InMemoryServiceRegistry;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManagerConfigurationContext;
import org.apereo.cas.support.saml.AbstractOpenSamlTests;
import org.apereo.cas.support.saml.authentication.SamlAuthenticationMetaDataPopulator;
import org.apereo.cas.support.saml.authentication.SamlResponseBuilder;
import org.apereo.cas.support.saml.authentication.principal.SamlServiceFactory;
import org.apereo.cas.support.saml.util.Saml10ObjectBuilder;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.validation.DefaultAssertionBuilder;
import org.apereo.cas.web.support.DefaultArgumentExtractor;
import org.apereo.cas.web.view.attributes.NoOpProtocolAttributesRenderer;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for {@link Saml10SuccessResponseView} class.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.1
 */
@Tag("SAML")
public class Saml10SuccessResponseViewTests extends AbstractOpenSamlTests {

    private static final String TEST_VALUE = "testValue";

    private static final String TEST_ATTRIBUTE = "testAttribute";

    private static final String PRINCIPAL_ID = "testPrincipal";

    private Saml10SuccessResponseView response;

    @BeforeEach
    public void initialize() {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();

        val list = new ArrayList<RegisteredService>();
        list.add(RegisteredServiceTestUtils.getRegisteredService("https://.+"));
        val dao = new InMemoryServiceRegistry(appCtx, list, new ArrayList<>());

        val context = ServicesManagerConfigurationContext.builder()
            .serviceRegistry(dao)
            .applicationContext(appCtx)
            .environments(new HashSet<>(0))
            .servicesCache(Caffeine.newBuilder().build())
            .build();
        val mgmr = new DefaultServicesManager(context);
        mgmr.load();

        val protocolAttributeEncoder = new DefaultCasProtocolAttributeEncoder(mgmr, CipherExecutor.noOpOfStringToString());
        val builder = new Saml10ObjectBuilder(configBean);
        val samlResponseBuilder = new SamlResponseBuilder(builder, "testIssuer", "whatever", 1000, 30,
            new NoOpProtocolAttributeEncoder(), mgmr);
        this.response = new Saml10SuccessResponseView(protocolAttributeEncoder,
            mgmr,
            new DefaultArgumentExtractor(new SamlServiceFactory()),
            StandardCharsets.UTF_8.name(),
            new DefaultAuthenticationAttributeReleasePolicy("attribute"),
            new DefaultAuthenticationServiceSelectionPlan(),
            NoOpProtocolAttributesRenderer.INSTANCE,
            samlResponseBuilder);
    }

    @Test
    public void verifyResponse() throws Exception {
        val model = new HashMap<String, Object>();

        val attributes = new HashMap<String, List<Object>>();
        attributes.put(TEST_ATTRIBUTE, List.of(TEST_VALUE));
        attributes.put("testEmptyCollection", new ArrayList<>(0));
        attributes.put("testAttributeCollection", Arrays.asList("tac1", "tac2"));
        val principal = PrincipalFactoryUtils.newPrincipalFactory().createPrincipal(PRINCIPAL_ID, attributes);

        val authAttributes = new HashMap<String, List<Object>>();
        authAttributes.put(
            SamlAuthenticationMetaDataPopulator.ATTRIBUTE_AUTHENTICATION_METHOD,
            List.of(SamlAuthenticationMetaDataPopulator.AUTHN_METHOD_SSL_TLS_CLIENT));
        authAttributes.put("testSamlAttribute", List.of("value"));

        val primary = CoreAuthenticationTestUtils.getAuthentication(principal, authAttributes);
        val assertion = new DefaultAssertionBuilder(primary).with(List.of(primary)).with(
            CoreAuthenticationTestUtils.getWebApplicationService()).with(true).build();
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

        val principal = PrincipalFactoryUtils.newPrincipalFactory().createPrincipal(PRINCIPAL_ID);

        val authAttributes = new HashMap<String, List<Object>>();
        authAttributes.put(
            SamlAuthenticationMetaDataPopulator.ATTRIBUTE_AUTHENTICATION_METHOD,
            List.of(SamlAuthenticationMetaDataPopulator.AUTHN_METHOD_SSL_TLS_CLIENT));
        authAttributes.put("testSamlAttribute", List.of("value"));

        val primary = CoreAuthenticationTestUtils.getAuthentication(principal, authAttributes);
        val assertion = new DefaultAssertionBuilder(primary)
            .with(List.of(primary))
            .with(CoreAuthenticationTestUtils.getWebApplicationService())
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

        val attributes = new HashMap<String, List<Object>>();
        attributes.put(TEST_ATTRIBUTE, List.of(TEST_VALUE));
        val principal = PrincipalFactoryUtils.newPrincipalFactory().createPrincipal(PRINCIPAL_ID, attributes);

        val authnAttributes = new HashMap<String, List<Object>>();
        authnAttributes.put("authnAttribute1", List.of("authnAttrbuteV1"));
        authnAttributes.put("authnAttribute2", List.of("authnAttrbuteV2"));
        authnAttributes.put(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME, List.of(Boolean.TRUE));

        val primary = CoreAuthenticationTestUtils.getAuthentication(principal, authnAttributes);

        val assertion = new DefaultAssertionBuilder(primary)
            .with(List.of(primary))
            .with(CoreAuthenticationTestUtils.getWebApplicationService())
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
