package org.apereo.cas.support.saml.web.idp.profile.artifact;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.ticket.artifact.SamlArtifactTicketFactory;

import lombok.val;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.saml2.core.Artifact;
import org.opensaml.saml.saml2.core.ArtifactResolve;
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
 * This is {@link SamlIdPSaml1ArtifactResolutionProfileHandlerControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SamlIdPSaml1ArtifactResolutionProfileHandlerControllerTests extends BaseSamlIdPConfigurationTests {
    @Autowired
    @Qualifier("saml1ArtifactResolutionController")
    private SamlIdPSaml1ArtifactResolutionProfileHandlerController controller;

    @Autowired
    @Qualifier("samlArtifactTicketFactory")
    private SamlArtifactTicketFactory samlArtifactTicketFactory;

    private SamlRegisteredService samlRegisteredService;

    @BeforeEach
    public void beforeEach() {
        samlRegisteredService = getSamlRegisteredServiceFor(false, false,
            false, "https://cassp.example.org");
        servicesManager.save(samlRegisteredService);
        ticketRegistry.deleteAll();
    }

    @Test
    @Order(1)
    public void verifyOK() {
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setContentType(MediaType.TEXT_XML_VALUE);

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
        val artifactResolve = getArtifactResolve();
        body.getUnknownXMLObjects().add(artifactResolve);
        envelope.setBody(body);

        val xml = SamlUtils.transformSamlObject(openSamlConfigBean, envelope).toString();
        request.setContent(xml.getBytes(StandardCharsets.UTF_8));

        val ticket = samlArtifactTicketFactory.create("https://cassp.example.org", CoreAuthenticationTestUtils.getAuthentication(),
            new MockTicketGrantingTicket("casuser"), "https://cas.example.org", "https://cassp.example.org",
            artifactResolve);
        ticketRegistry.addTicket(ticket);
        controller.handlePostRequest(response, request);
        assertEquals(HttpStatus.SC_OK, response.getStatus());
    }

    @Test
    @Order(2)
    public void verifyFault() {
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setContentType(MediaType.TEXT_XML_VALUE);

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
        val artifactResolve = getArtifactResolve();
        body.getUnknownXMLObjects().add(artifactResolve);
        envelope.setBody(body);

        val xml = SamlUtils.transformSamlObject(openSamlConfigBean, envelope).toString();
        request.setContent(xml.getBytes(StandardCharsets.UTF_8));
        controller.handlePostRequest(response, request);
        assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatus());
    }

    private ArtifactResolve getArtifactResolve() {
        var builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(ArtifactResolve.DEFAULT_ELEMENT_NAME);
        val request = (ArtifactResolve) builder.buildObject();
        builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
        val issuer = (Issuer) builder.buildObject();
        issuer.setValue(samlRegisteredService.getServiceId());
        request.setIssuer(issuer);


        builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(Artifact.DEFAULT_ELEMENT_NAME);
        val artifact = (Artifact) builder.buildObject();
        artifact.setValue("https://cassp.example.org");
        request.setArtifact(artifact);
        return request;

    }
}
