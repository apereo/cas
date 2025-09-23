package org.apereo.cas.support.saml.web.idp.profile.sso;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.web.idp.profile.slo.SamlIdPHttpRedirectDeflateEncoder;
import org.apereo.cas.util.EncodingUtils;
import lombok.val;
import net.shibboleth.shared.net.URLBuilder;
import org.apache.commons.lang3.Strings;
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
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SSOSamlIdPPostSimpleSignProfileHandlerControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML2Web")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestPropertySource(properties = "cas.authn.saml-idp.metadata.file-system.location=file:src/test/resources/metadata")
class SSOSamlIdPPostSimpleSignProfileHandlerControllerTests extends BaseSamlIdPConfigurationTests {
    @Autowired
    @Qualifier("ssoPostSimpleSignProfileHandlerController")
    private SSOSamlIdPPostSimpleSignProfileHandlerController controller;

    private SamlRegisteredService samlRegisteredService;

    @BeforeEach
    void beforeEach() {
        samlRegisteredService = getSamlRegisteredServiceFor(false, false,
            false, "https://cassp.example.org");
        servicesManager.save(samlRegisteredService);
    }

    @Test
    @Order(1)
    void verifyPostSignRequest() throws Throwable {
        val request = new MockHttpServletRequest();
        request.setMethod("POST");
        val response = new MockHttpServletResponse();

        val authnRequest = signAuthnRequest(request, response, getAuthnRequest(), samlRegisteredService);
        val xml = SamlUtils.transformSamlObject(openSamlConfigBean, authnRequest).toString();
        request.addParameter(SamlProtocolConstants.PARAMETER_SAML_REQUEST, EncodingUtils.encodeBase64(xml));
        val mv = controller.handleSaml2ProfileSsoPostRequest(response, request);
        assertEquals(HttpStatus.FOUND, mv.getStatus());
    }

    @Test
    @Order(2)
    void verifyRedirectRequest() throws Throwable {
        val request = new MockHttpServletRequest();
        request.setMethod("GET");
        val response = new MockHttpServletResponse();
        val authnRequest = signAuthnRequest(request, response, getAuthnRequest(), samlRegisteredService);

        val encoder = new SamlIdPHttpRedirectDeflateEncoder("https://cas.example.org/login", authnRequest);
        encoder.doEncode();
        val queryStrings = Strings.CI.remove(encoder.getRedirectUrl(), "https://cas.example.org/login?");
        new URLBuilder(encoder.getRedirectUrl())
            .getQueryParams().forEach(param -> request.addParameter(param.getFirst(), param.getSecond()));
        request.setQueryString(queryStrings);

        val mv = controller.handleSaml2ProfileSsoRedirectRequest(response, request);
        assertEquals(HttpStatus.FOUND, mv.getStatus());
    }

    @Test
    @Order(2)
    void verifyBadRequest() {
        val request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.addParameter(SamlProtocolConstants.PARAMETER_SAML_REQUEST, "Text");
        val response = new MockHttpServletResponse();
        val mv = controller.handleSaml2ProfileSsoPostRequest(response, request);
        assertEquals(HttpStatus.BAD_REQUEST, mv.getStatus());
    }

    private AuthnRequest getAuthnRequest() {
        var builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(AuthnRequest.DEFAULT_ELEMENT_NAME);
        val authnRequest = (AuthnRequest) builder.buildObject();
        authnRequest.setProtocolBinding(SAMLConstants.SAML2_POST_SIMPLE_SIGN_BINDING_URI);
        builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
        val issuer = (Issuer) builder.buildObject();
        issuer.setValue(samlRegisteredService.getServiceId());
        authnRequest.setIssuer(issuer);
        return authnRequest;
    }
}
