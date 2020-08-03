package org.apereo.cas.support.saml.web.idp.profile.ecp;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.HttpUtils;

import lombok.val;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.soap.common.SOAPObjectBuilder;
import org.opensaml.soap.soap11.Body;
import org.opensaml.soap.soap11.Envelope;
import org.opensaml.soap.soap11.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ECPSamlIdPProfileHandlerControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ECPSamlIdPProfileHandlerControllerTests extends BaseSamlIdPConfigurationTests {
    @Autowired
    @Qualifier("ecpProfileHandlerController")
    private ECPSamlIdPProfileHandlerController controller;

    private SamlRegisteredService samlRegisteredService;

    @BeforeEach
    public void beforeEach() {
        this.samlRegisteredService = getSamlRegisteredServiceFor(false, false,
            false, "https://cassp.example.org");
        servicesManager.save(samlRegisteredService);
    }

    @Test
    @Order(1)
    public void verifyOK() {
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setContentType(MediaType.TEXT_XML_VALUE);

        val headers = HttpUtils.createBasicAuthHeaders("casuser", "casuser");
        headers.forEach(request::addHeader);
        val envelope = getEnvelope();
        val xml = SamlUtils.transformSamlObject(openSamlConfigBean, envelope).toString();
        request.setContent(xml.getBytes(StandardCharsets.UTF_8));

        controller.handleEcpRequest(response, request);
        assertEquals(HttpStatus.SC_OK, response.getStatus());
    }

    @Test
    @Order(2)
    public void verifyBadAuthn() {
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setContentType(MediaType.TEXT_XML_VALUE);

        val headers = HttpUtils.createBasicAuthHeaders("xyz", "123");
        headers.forEach(request::addHeader);
        val envelope = getEnvelope();
        val xml = SamlUtils.transformSamlObject(openSamlConfigBean, envelope).toString();
        request.setContent(xml.getBytes(StandardCharsets.UTF_8));

        controller.handleEcpRequest(response, request);
        assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatus());
    }

    @Test
    @Order(3)
    public void verifyNoCredentials() {
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setContentType(MediaType.TEXT_XML_VALUE);
        val envelope = getEnvelope();
        val xml = SamlUtils.transformSamlObject(openSamlConfigBean, envelope).toString();
        request.setContent(xml.getBytes(StandardCharsets.UTF_8));

        controller.handleEcpRequest(response, request);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, response.getStatus());
    }

    private Envelope getEnvelope() {
        var builder = (SOAPObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(Envelope.DEFAULT_ELEMENT_NAME);
        var envelope = (Envelope) builder.buildObject();

        builder = (SOAPObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(Header.DEFAULT_ELEMENT_NAME);
        val header = (Header) builder.buildObject();
        envelope.setHeader(header);

        builder = (SOAPObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(Body.DEFAULT_ELEMENT_NAME);
        val body = (Body) builder.buildObject();
        body.getUnknownXMLObjects().add(getAuthnRequest());
        envelope.setBody(body);
        return envelope;
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
