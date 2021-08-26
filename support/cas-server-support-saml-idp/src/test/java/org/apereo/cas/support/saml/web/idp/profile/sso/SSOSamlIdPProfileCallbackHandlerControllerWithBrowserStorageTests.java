package org.apereo.cas.support.saml.web.idp.profile.sso;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.authentication.SamlIdPAuthenticationContext;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.web.BrowserSessionStorage;
import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.val;
import org.apache.http.HttpStatus;
import org.jasig.cas.client.authentication.AttributePrincipalImpl;
import org.jasig.cas.client.validation.AssertionImpl;
import org.jasig.cas.client.validation.TicketValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.pac4j.core.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SSOSamlIdPProfileCallbackHandlerControllerWithBrowserStorageTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Import(SSOSamlIdPProfileCallbackHandlerControllerWithBrowserStorageTests.SamlIdPTestConfiguration.class)
@Tag("SAML")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestPropertySource(properties = {
    "cas.authn.saml-idp.core.session-storage-type=BROWSER_SESSION_STORAGE",
    "cas.authn.saml-idp.metadata.file-system.location=file:src/test/resources/metadata"
})
public class SSOSamlIdPProfileCallbackHandlerControllerWithBrowserStorageTests extends BaseSamlIdPConfigurationTests {
    @Autowired
    @Qualifier("ssoPostProfileCallbackHandlerController")
    private SSOSamlIdPProfileCallbackHandlerController controller;

    private SamlRegisteredService samlRegisteredService;

    @BeforeEach
    public void beforeEach() {
        samlRegisteredService = getSamlRegisteredServiceFor(false, false,
            false, "https://cassp.example.org");
        servicesManager.save(samlRegisteredService);
    }

    @Test
    public void verifyReadFromStorage() throws Exception {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val authn = getAuthnRequest();
        authn.setProtocolBinding(SAMLConstants.SAML2_POST_BINDING_URI);
        val xml = SamlUtils.transformSamlObject(openSamlConfigBean, getAuthnRequest()).toString();
        request.getSession().setAttribute(SamlProtocolConstants.PARAMETER_SAML_REQUEST, EncodingUtils.encodeBase64(xml));
        request.getSession().setAttribute(SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE, UUID.randomUUID().toString());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, "ST-1234567890");
        val mv = controller.handleCallbackProfileRequestGet(response, request);
        assertEquals(CasWebflowConstants.VIEW_ID_SESSION_STORAGE_READ, mv.getViewName());
    }

    @Test
    public void verifyResumeFromStorage() throws Exception {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val authn = getAuthnRequest();
        authn.setProtocolBinding(SAMLConstants.SAML2_POST_BINDING_URI);
        val xml = SamlUtils.transformSamlObject(openSamlConfigBean, getAuthnRequest()).toString();
        request.getSession().setAttribute(SamlProtocolConstants.PARAMETER_SAML_REQUEST, EncodingUtils.encodeBase64(xml));
        request.getSession().setAttribute(SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE, UUID.randomUUID().toString());
        val context = new MessageContext();
        context.setMessage(getAuthnRequest());
        request.getSession().setAttribute(MessageContext.class.getName(), SamlIdPAuthenticationContext.from(context).encode());

        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, "ST-1234567890");
        val payload = samlIdPDistributedSessionStore.getTrackableSession(new JEEContext(request, response))
            .map(BrowserSessionStorage.class::cast)
            .map(BrowserSessionStorage::getPayload)
            .orElseThrow();
        request.addParameter(BrowserSessionStorage.KEY_SESSION_STORAGE, payload);
        val mv = controller.handleCallbackProfileRequestPost(response, request);
        assertNull(mv);
        assertEquals(HttpStatus.SC_OK, response.getStatus());
    }

    @TestConfiguration
    @Lazy(false)
    public static class SamlIdPTestConfiguration {

        @Bean
        public TicketValidator samlIdPTicketValidator() throws Exception {
            val validator = mock(TicketValidator.class);
            val principal = new AttributePrincipalImpl("casuser", CollectionUtils.wrap("cn", "cas"));
            when(validator.validate(anyString(), anyString())).thenReturn(new AssertionImpl(principal));
            return validator;
        }
    }

    private AuthnRequest getAuthnRequest() {
        var builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(AuthnRequest.DEFAULT_ELEMENT_NAME);
        var authnRequest = (AuthnRequest) builder.buildObject();
        builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
        val issuer = (Issuer) builder.buildObject();
        issuer.setValue(samlRegisteredService.getServiceId());
        authnRequest.setIssuer(issuer);
        return authnRequest;
    }
}
