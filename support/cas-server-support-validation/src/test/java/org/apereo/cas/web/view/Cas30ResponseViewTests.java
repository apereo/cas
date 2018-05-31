package org.apereo.cas.web.view;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CasViewConstants;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationAttributeReleasePolicy;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.DefaultMultifactorAuthenticationContextValidator;
import org.apereo.cas.authentication.DefaultMultifactorTriggerSelectionStrategy;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.support.DefaultCasProtocolAttributeEncoder;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.web.view.AbstractCasView;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.crypto.PrivateKeyFactoryBean;
import org.apereo.cas.validation.DefaultServiceTicketValidationAuthorizersExecutionPlan;
import org.apereo.cas.web.AbstractServiceValidateController;
import org.apereo.cas.web.AbstractServiceValidateControllerTests;
import org.apereo.cas.web.ServiceValidateController;
import org.apereo.cas.web.view.attributes.DefaultCas30ProtocolAttributesRenderer;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.support.RequestContext;

import javax.crypto.Cipher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link Cas30ResponseView}.
 *
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@DirtiesContext
@TestPropertySource(properties = {"cas.clearpass.cacheCredential=true", "cas.clearpass.crypto.enabled=false"})
@Slf4j
public class Cas30ResponseViewTests extends AbstractServiceValidateControllerTests {
    
    @Autowired
    @Qualifier("servicesManager")
    protected ServicesManager servicesManager;

    @Autowired
    @Qualifier("cas3ServiceJsonView")
    private View cas3ServiceJsonView;

    @Autowired
    @Qualifier("cas3SuccessView")
    private View cas3SuccessView;

    @Autowired
    @Qualifier("cas3ServiceFailureView")
    private View cas3ServiceFailureView;

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @Override
    public AbstractServiceValidateController getServiceValidateControllerInstance() {
        return new ServiceValidateController(
            getValidationSpecification(),
            getAuthenticationSystemSupport(), getServicesManager(),
            getCentralAuthenticationService(),
            getProxyHandler(),
            getArgumentExtractor(),
            new DefaultMultifactorTriggerSelectionStrategy(new MultifactorAuthenticationProperties()),
            new DefaultMultifactorAuthenticationContextValidator("", "OPEN", "test", applicationContext),
            cas3ServiceJsonView,
            cas3SuccessView,
            cas3ServiceFailureView,
            "authenticationContext",
            new DefaultServiceTicketValidationAuthorizersExecutionPlan(),
            true
        );
    }

    protected Map<?, ?> renderView() throws Exception {
        final var modelAndView = this.getModelAndViewUponServiceValidationWithSecurePgtUrl();
        LOGGER.debug("Retrieved model and view [{}]", modelAndView.getModel());

        final var req = new MockHttpServletRequest(new MockServletContext());
        req.setAttribute(RequestContext.WEB_APPLICATION_CONTEXT_ATTRIBUTE, new GenericWebApplicationContext(req.getServletContext()));

        final ProtocolAttributeEncoder encoder = new DefaultCasProtocolAttributeEncoder(this.servicesManager, CipherExecutor.noOpOfStringToString());
        final View viewDelegated = new View() {
            @Override
            public String getContentType() {
                return MediaType.TEXT_HTML_VALUE;
            }

            @Override
            public void render(final Map<String, ?> map, final HttpServletRequest request, final HttpServletResponse response) {
                LOGGER.warn("Setting attribute [{}]", map.keySet());
                map.forEach(request::setAttribute);
            }
        };

        final var view = getCasViewToRender(encoder, viewDelegated);
        final var resp = new MockHttpServletResponse();
        view.render(modelAndView.getModel(), req, resp);
        return getRenderedViewModelMap(req);
    }

    protected Map getRenderedViewModelMap(final MockHttpServletRequest req) {
        return (Map) req.getAttribute(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_ATTRIBUTES);
    }

    protected AbstractCasView getCasViewToRender(final ProtocolAttributeEncoder encoder, final View viewDelegated) {
        return new Cas30ResponseView(true, encoder, servicesManager,
            "attribute",
            viewDelegated, true, new DefaultAuthenticationAttributeReleasePolicy(),
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()),
            new DefaultCas30ProtocolAttributesRenderer());
    }

    @Test
    public void verifyViewAuthnAttributes() throws Exception {
        final Map<?, ?> attributes = renderView();
        assertTrue(attributes.containsKey(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_AUTHENTICATION_DATE));
        assertTrue(attributes.containsKey(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_FROM_NEW_LOGIN));
        assertTrue(attributes.containsKey(CasProtocolConstants.VALIDATION_REMEMBER_ME_ATTRIBUTE_NAME));
    }

    @Test
    public void verifyPasswordAsAuthenticationAttributeCanDecrypt() throws Exception {
        final Map<?, ?> attributes = renderView();
        assertTrue(attributes.containsKey(CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL));

        final var encodedPsw = (String) attributes.get(CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL);
        final var password = decryptCredential(encodedPsw);
        final var creds = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        assertEquals(password, creds.getPassword());
    }

    @Test
    public void verifyProxyGrantingTicketAsAuthenticationAttributeCanDecrypt() throws Exception {
        final Map<?, ?> attributes = renderView();
        LOGGER.warn("Attributes are [{}]", attributes.keySet());
        assertTrue(attributes.containsKey(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET));

        final var encodedPgt = (String) attributes.get(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET);
        final var pgt = decryptCredential(encodedPgt);
        assertNotNull(pgt);
    }

    @SneakyThrows
    private String decryptCredential(final String cred) {
        final var factory = new PrivateKeyFactoryBean();
        factory.setAlgorithm("RSA");
        factory.setLocation(new ClassPathResource("keys/RSA4096Private.p8"));
        factory.setSingleton(false);
        final var privateKey = factory.getObject();

        LOGGER.debug("Initializing cipher based on [{}]", privateKey.getAlgorithm());
        final var cipher = Cipher.getInstance(privateKey.getAlgorithm());

        LOGGER.debug("Decoding value [{}]", cred);
        final var cred64 = EncodingUtils.decodeBase64(cred);

        LOGGER.debug("Initializing decrypt-mode via private key [{}]", privateKey.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        final var cipherData = cipher.doFinal(cred64);
        return new String(cipherData, StandardCharsets.UTF_8);
    }
}
