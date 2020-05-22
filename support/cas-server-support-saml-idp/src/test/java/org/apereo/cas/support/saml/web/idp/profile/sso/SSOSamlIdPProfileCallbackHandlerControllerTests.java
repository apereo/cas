package org.apereo.cas.support.saml.web.idp.profile.sso;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;

import lombok.val;
import org.apache.http.HttpStatus;
import org.jasig.cas.client.authentication.AttributePrincipalImpl;
import org.jasig.cas.client.validation.AbstractUrlBasedTicketValidator;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.AssertionImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URL;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SSOSamlIdPProfileCallbackHandlerControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Import(SSOSamlIdPProfileCallbackHandlerControllerTests.SamlIdPTestConfiguration.class)
@Tag("SAML")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestPropertySource(properties = "cas.authn.saml-idp.metadata.location=file:src/test/resources/metadata")
public class SSOSamlIdPProfileCallbackHandlerControllerTests extends BaseSamlIdPConfigurationTests {
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
    public void verifyNoRequest() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        assertThrows(IllegalArgumentException.class, () -> controller.handleCallbackProfileRequest(response, request));
    }

    @Test
    public void verifyNoTicket() throws Exception {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val authnRequest = signAuthnRequest(request, response, getAuthnRequest());
        val xml = SamlUtils.transformSamlObject(openSamlConfigBean, authnRequest).toString();
        request.addParameter(SamlProtocolConstants.PARAMETER_SAML_REQUEST, EncodingUtils.encodeBase64(xml));

        controller.handleCallbackProfileRequest(response, request);
        assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatus());
    }

    @Test
    @Order(1)
    public void verifyValidationByPost() throws Exception {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val authn = getAuthnRequest();
        authn.setProtocolBinding(SAMLConstants.SAML2_POST_BINDING_URI);
        val authnRequest = signAuthnRequest(request, response, authn);
        val xml = SamlUtils.transformSamlObject(openSamlConfigBean, authnRequest).toString();
        request.addParameter(SamlProtocolConstants.PARAMETER_SAML_REQUEST, EncodingUtils.encodeBase64(xml));
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, "ST-1234567890");
        request.addParameter(SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE, UUID.randomUUID().toString());
        controller.handleCallbackProfileRequest(response, request);
        assertEquals(HttpStatus.SC_OK, response.getStatus());
    }

    @Test
    @Order(2)
    public void verifyValidationByRedirect() throws Exception {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val authn = getAuthnRequest();
        authn.setProtocolBinding(SAMLConstants.SAML2_POST_SIMPLE_SIGN_BINDING_URI);
        val authnRequest = signAuthnRequest(request, response, authn);
        val xml = SamlUtils.transformSamlObject(openSamlConfigBean, authnRequest).toString();
        request.addParameter(SamlProtocolConstants.PARAMETER_SAML_REQUEST, EncodingUtils.encodeBase64(xml));
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, "ST-1234567890");
        request.addParameter(SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE, UUID.randomUUID().toString());
        controller.handleCallbackProfileRequest(response, request);
        assertEquals(HttpStatus.SC_OK, response.getStatus());
    }

    private AuthnRequest signAuthnRequest(final HttpServletRequest request,
                                          final HttpServletResponse response,
                                          final AuthnRequest authnRequest) {
        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade
            .get(samlRegisteredServiceCachingMetadataResolver, samlRegisteredService,
                samlRegisteredService.getServiceId()).get();
        return samlIdPObjectSigner.encode(authnRequest, samlRegisteredService,
            adaptor, response, request, SAMLConstants.SAML2_POST_BINDING_URI, authnRequest);
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

    @TestConfiguration
    @Lazy(false)
    public static class SamlIdPTestConfiguration {

        @Bean
        public AbstractUrlBasedTicketValidator casClientTicketValidator() {
            return new AbstractUrlBasedTicketValidator("https://cas.example.org") {
                @Override
                protected String getUrlSuffix() {
                    return "/cas";
                }

                @Override
                protected Assertion parseResponseFromServer(final String s) {
                    return new AssertionImpl(new AttributePrincipalImpl("casuser", CollectionUtils.wrap("cn", "cas")));
                }

                @Override
                protected String retrieveResponseFromServer(final URL url, final String s) {
                    return "the-response";
                }
            };
        }
    }
}
