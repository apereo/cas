package org.apereo.cas.web.view;

import module java.base;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CasViewConstants;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationAttributeReleasePolicy;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.support.DefaultCasProtocolAttributeEncoder;
import org.apereo.cas.config.CasThymeleafAutoConfiguration;
import org.apereo.cas.config.CasValidationAutoConfiguration;
import org.apereo.cas.services.RegisteredServicePublicKeyCipherExecutor;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.web.view.AbstractCasView;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.crypto.PrivateKeyFactoryBean;
import org.apereo.cas.web.AbstractServiceValidateController;
import org.apereo.cas.web.AbstractServiceValidateControllerTests;
import org.apereo.cas.web.MockRequestedAuthenticationContextValidator;
import org.apereo.cas.web.ServiceValidateConfigurationContext;
import org.apereo.cas.web.ServiceValidationViewFactory;
import org.apereo.cas.web.v2.ServiceValidateController;
import org.apereo.cas.web.view.attributes.DefaultCas30ProtocolAttributesRenderer;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.support.RequestContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Cas30ResponseView}.
 *
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@Slf4j
@TestPropertySource(properties = {
    "cas.clearpass.cache-credential=true",
    "cas.clearpass.crypto.enabled=false"
})
@ImportAutoConfiguration({
    CasThymeleafAutoConfiguration.class,
    CasValidationAutoConfiguration.class
})
@Tag("CAS")
@ExtendWith(CasTestExtension.class)
class Cas30ResponseViewTests extends AbstractServiceValidateControllerTests {

    @Autowired
    @Qualifier(ServiceValidationViewFactory.BEAN_NAME)
    protected ServiceValidationViewFactory serviceValidationViewFactory;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    protected ServicesManager servicesManager;

    private static String decryptCredential(final String cred) throws Throwable {
        val factory = new PrivateKeyFactoryBean();
        factory.setAlgorithm("RSA");
        factory.setLocation(new ClassPathResource("keys/RSA4096Private.p8"));
        factory.setSingleton(false);
        val privateKey = factory.getObject();

        LOGGER.debug("Initializing cipher based on [{}]", Objects.requireNonNull(privateKey).getAlgorithm());
        val cipher = Cipher.getInstance(privateKey.getAlgorithm());

        LOGGER.debug("Decoding value [{}]", cred);
        val cred64 = EncodingUtils.decodeBase64(cred);

        LOGGER.debug("Initializing decrypt-mode via private key [{}]", privateKey.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        val cipherData = cipher.doFinal(cred64);
        return new String(cipherData, StandardCharsets.UTF_8);
    }

    protected static View getDelegatedView() {
        return new View() {
            @Override
            public String getContentType() {
                return MediaType.TEXT_HTML_VALUE;
            }

            @Override
            public void render(final Map<String, ?> map,
                               @NonNull
                               final HttpServletRequest request,
                               @NonNull
                               final HttpServletResponse response) {
                LOGGER.warn("Setting attribute [{}]", map.keySet());
                map.forEach(request::setAttribute);
            }
        };
    }

    @Override
    public AbstractServiceValidateController getServiceValidateControllerInstance() {
        val context = ServiceValidateConfigurationContext.builder()
            .applicationContext(applicationContext)
            .principalFactory(getPrincipalFactory())
            .ticketRegistry(getTicketRegistry())
            .principalResolver(getDefaultPrincipalResolver())
            .validationSpecifications(CollectionUtils.wrapSet(getValidationSpecification()))
            .authenticationSystemSupport(getAuthenticationSystemSupport())
            .servicesManager(getServicesManager())
            .centralAuthenticationService(getCentralAuthenticationService())
            .argumentExtractor(getArgumentExtractor())
            .proxyHandler(getProxyHandler())
            .requestedContextValidator(new MockRequestedAuthenticationContextValidator())
            .validationAuthorizers(getServiceValidationAuthorizers())
            .validationViewFactory(serviceValidationViewFactory)
            .casProperties(casProperties)
            .serviceFactory(getWebApplicationServiceFactory())
            .build();
        return new ServiceValidateController(context);
    }

    @Test
    void verifyViewAuthnAttributes() throws Throwable {
        val attributes = renderView();
        assertTrue(attributes.containsKey(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_AUTHENTICATION_DATE));
        assertTrue(attributes.containsKey(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_FROM_NEW_LOGIN));
        assertTrue(attributes.containsKey(CasProtocolConstants.VALIDATION_REMEMBER_ME_ATTRIBUTE_NAME));
    }

    @Test
    void verifyPasswordAsAuthenticationAttributeCanDecrypt() throws Throwable {
        val attributes = renderView();
        assertTrue(attributes.containsKey(CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL));

        val encodedPsw = (String) attributes.get(CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL);
        val password = decryptCredential(encodedPsw);
        val creds = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        assertEquals(password, creds.toPassword());
    }

    @Test
    void verifyProxyGrantingTicketAsAuthenticationAttributeCanDecrypt() throws Throwable {
        val attributes = renderView();
        LOGGER.trace("Attributes are [{}]", attributes.keySet());
        assertTrue(attributes.containsKey(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET));

        val encodedPgt = (String) attributes.get(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET);
        assertNotNull(encodedPgt);
        val pgt = decryptCredential(encodedPgt);
        assertNotNull(pgt);
    }

    @Test
    void verifyViewBinaryAttributes() throws Throwable {
        val attributes = renderView();
        assertTrue(attributes.containsKey("binaryAttribute"));
        val binaryAttr = attributes.get("binaryAttribute");
        assertEquals("binaryAttributeValue", EncodingUtils.decodeBase64ToString(binaryAttr.toString()));
    }

    protected Map<?, ?> renderView() throws Throwable {
        val modelAndView = this.getModelAndViewUponServiceValidationWithSecurePgtUrl(DEFAULT_SERVICE);
        LOGGER.debug("Retrieved model and view [{}]", modelAndView.getModel());

        val req = new MockHttpServletRequest(new MockServletContext());
        req.setAttribute(RequestContext.WEB_APPLICATION_CONTEXT_ATTRIBUTE, new GenericWebApplicationContext(req.getServletContext()));

        val encoder = new DefaultCasProtocolAttributeEncoder(this.servicesManager,
            RegisteredServicePublicKeyCipherExecutor.INSTANCE, CipherExecutor.noOpOfStringToString());
        val view = getCasViewToRender(encoder, getDelegatedView());
        val resp = new MockHttpServletResponse();
        view.render(modelAndView.getModel(), req, resp);
        return getRenderedViewModelMap(req);
    }

    protected Map getRenderedViewModelMap(final MockHttpServletRequest req) {
        return (Map) req.getAttribute(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_ATTRIBUTES);
    }

    protected AbstractCasView getCasViewToRender(final ProtocolAttributeEncoder encoder, final View viewDelegated) {
        return new Cas30ResponseView(true, encoder, servicesManager,
            viewDelegated, new DefaultAuthenticationAttributeReleasePolicy("attribute"),
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()),
            new DefaultCas30ProtocolAttributesRenderer(), getAttributeDefinitionStore());
    }
}
