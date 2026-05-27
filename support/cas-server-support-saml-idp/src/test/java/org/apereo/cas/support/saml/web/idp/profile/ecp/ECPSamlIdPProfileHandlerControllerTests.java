package org.apereo.cas.support.saml.web.idp.profile.ecp;

import module java.base;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.http.HttpUtils;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.soap.common.SOAPObjectBuilder;
import org.opensaml.soap.soap11.Body;
import org.opensaml.soap.soap11.Envelope;
import org.opensaml.soap.soap11.FaultString;
import org.opensaml.soap.soap11.Header;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * This is {@link ECPSamlIdPProfileHandlerControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML2Web")
class ECPSamlIdPProfileHandlerControllerTests extends BaseSamlIdPConfigurationTests {
    private SamlRegisteredService samlRegisteredService;

    @BeforeEach
    void beforeEach() {
        servicesManager.deleteAll();
        samlRegisteredService = getSamlRegisteredServiceFor(false, false,
            false, "https://cassp.example.org");
        servicesManager.save(samlRegisteredService);
    }

    @Test
    void verifyOK() throws Exception {
        val headers = HttpUtils.createBasicAuthHeaders("casuser", "casuser");
        val envelope = getEnvelope(samlRegisteredService.getServiceId());
        val xml = SamlUtils.transformSamlObject(openSamlConfigBean, envelope).toString();
        val result = performEcpRequest(xml, headers);
        assertEquals(HttpStatus.SC_OK, result.getResponse().getStatus());
    }

    @Test
    void verifyBadAuthn() throws Exception {
        val headers = HttpUtils.createBasicAuthHeaders("xyz", "123");
        val envelope = getEnvelope(samlRegisteredService.getServiceId());
        val xml = SamlUtils.transformSamlObject(openSamlConfigBean, envelope).toString();
        val result = performEcpRequest(xml, headers);
        assertEquals(HttpStatus.SC_OK, result.getResponse().getStatus());
        assertNotNull(result.getRequest().getAttribute(SamlIdPConstants.REQUEST_ATTRIBUTE_ERROR));
        assertNotNull(result.getRequest().getAttribute(FaultString.class.getSimpleName()));
    }

    @Test
    void verifyNoCredentials() throws Exception {
        val envelope = getEnvelope(samlRegisteredService.getServiceId());
        val xml = SamlUtils.transformSamlObject(openSamlConfigBean, envelope).toString();
        val result = performEcpRequest(xml, HttpHeaders.EMPTY);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, result.getResponse().getStatus());
    }

    @Test
    void verifyFailures() throws Exception {
        val headers = HttpUtils.createBasicAuthHeaders("casuser", "casuser");
        val emptyResult = performEcpRequest(StringUtils.EMPTY, headers);
        assertEquals(HttpStatus.SC_OK, emptyResult.getResponse().getStatus());

        val envelope = getEnvelope(UUID.randomUUID().toString());
        val xml = SamlUtils.transformSamlObject(openSamlConfigBean, envelope).toString();
        val result = performEcpRequest(xml, headers);
        assertEquals(HttpStatus.SC_OK, result.getResponse().getStatus());
        assertNotNull(result.getRequest().getAttribute(SamlIdPConstants.REQUEST_ATTRIBUTE_ERROR));
        assertNotNull(result.getRequest().getAttribute(FaultString.class.getSimpleName()));
    }

    private MvcResult performEcpRequest(final String xml, final HttpHeaders headers) throws Exception {
        val builder = post(SamlIdPConstants.ENDPOINT_SAML2_IDP_ECP_PROFILE_SSO)
            .contentType(MediaType.TEXT_XML)
            .accept(MediaType.TEXT_XML, MediaType.valueOf(SamlIdPConstants.ECP_SOAP_PAOS_CONTENT_TYPE));
        if (headers != null) {
            headers.forEach((key, values) -> values.forEach(value -> builder.header(key, value)));
        }
        if (StringUtils.isNotBlank(xml)) {
            builder.content(xml.getBytes(StandardCharsets.UTF_8));
        }
        return mockMvc.perform(builder).andReturn();
    }


    private Envelope getEnvelope(final String entityId) {
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
        body.getUnknownXMLObjects().add(getAuthnRequest(entityId));
        envelope.setBody(body);
        return envelope;
    }

    private AuthnRequest getAuthnRequest(final String entityId) {
        var builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(AuthnRequest.DEFAULT_ELEMENT_NAME);
        var authnRequest = (AuthnRequest) builder.buildObject();
        authnRequest.setProtocolBinding(SAMLConstants.SAML2_PAOS_BINDING_URI);
        builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
        val issuer = (Issuer) builder.buildObject();
        issuer.setValue(entityId);
        authnRequest.setIssuer(issuer);
        return authnRequest;
    }

}
