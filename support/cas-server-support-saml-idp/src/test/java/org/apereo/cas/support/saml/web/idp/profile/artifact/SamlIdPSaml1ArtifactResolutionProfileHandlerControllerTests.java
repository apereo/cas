package org.apereo.cas.support.saml.web.idp.profile.artifact;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.ticket.artifact.SamlArtifactTicketFactory;
import lombok.val;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.Artifact;
import org.opensaml.saml.saml2.core.ArtifactResolve;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.soap.common.SOAPObjectBuilder;
import org.opensaml.soap.soap11.Body;
import org.opensaml.soap.soap11.Envelope;
import org.opensaml.soap.soap11.FaultString;
import org.opensaml.soap.soap11.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * This is {@link SamlIdPSaml1ArtifactResolutionProfileHandlerControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML2Web")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestPropertySource(properties = "cas.authn.saml-idp.metadata.file-system.location=file:src/test/resources/metadata")
class SamlIdPSaml1ArtifactResolutionProfileHandlerControllerTests extends BaseSamlIdPConfigurationTests {
    @Autowired
    @Qualifier("samlArtifactTicketFactory")
    private SamlArtifactTicketFactory samlArtifactTicketFactory;

    private SamlRegisteredService samlRegisteredService;

    @BeforeEach
    void beforeEach() {
        samlRegisteredService = getSamlRegisteredServiceFor(false, false,
            false, "https://cassp.example.org");
        servicesManager.save(samlRegisteredService);
        ticketRegistry.deleteAll();
    }

    @Test
    @Order(1)
    void verifyOK() throws Exception {
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
        val artifactResolve = getSignedArtifactResolve();
        body.getUnknownXMLObjects().add(artifactResolve);
        envelope.setBody(body);

        val xml = SamlUtils.transformSamlObject(openSamlConfigBean, envelope).toString();

        val ticket = samlArtifactTicketFactory.create("https://cassp.example.org",
            CoreAuthenticationTestUtils.getAuthentication(),
            new MockTicketGrantingTicket("casuser"), "https://cas.example.org",
            "https://cassp.example.org", artifactResolve);
        ticketRegistry.addTicket(ticket);
        val result = performSoapPost(xml);
        assertEquals(HttpStatus.SC_OK, result.getResponse().getStatus());
        assertNull(result.getRequest().getAttribute(FaultString.class.getSimpleName()));
        assertNull(ticketRegistry.getTicket(ticket.getId()));

        val replay = performSoapPost(xml);
        assertNotNull(replay.getRequest().getAttribute(FaultString.class.getSimpleName()));
    }

    @Test
    @Order(2)
    void verifyFault() throws Exception {
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
        val artifactResolve = getSignedArtifactResolve();
        body.getUnknownXMLObjects().add(artifactResolve);
        envelope.setBody(body);

        val xml = SamlUtils.transformSamlObject(openSamlConfigBean, envelope).toString();
        val result = performSoapPost(xml);
        assertEquals(HttpStatus.SC_OK, result.getResponse().getStatus());
        assertNotNull(result.getRequest().getAttribute(FaultString.class.getSimpleName()));
    }

    @Test
    void verifyUnsignedResolveRejectedRegardlessOfAuthnRequestRule() throws Exception {
        val envelope = SamlUtils.newSoapObject(Envelope.class);
        val body = SamlUtils.newSoapObject(Body.class);
        body.getUnknownXMLObjects().add(getArtifactResolve());
        envelope.setBody(body);

        val xml = SamlUtils.transformSamlObject(openSamlConfigBean, envelope).toString();
        val result = performSoapPost(xml);
        assertNotNull(result.getRequest().getAttribute(FaultString.class.getSimpleName()));
    }

    @Test
    void verifyArtifactCannotBeRedeemedByDifferentRelyingParty() throws Exception {
        val artifactResolve = getSignedArtifactResolve();
        val envelope = SamlUtils.newSoapObject(Envelope.class);
        val body = SamlUtils.newSoapObject(Body.class);
        body.getUnknownXMLObjects().add(artifactResolve);
        envelope.setBody(body);

        val ticket = samlArtifactTicketFactory.create(artifactResolve.getArtifact().getValue(),
            CoreAuthenticationTestUtils.getAuthentication(), new MockTicketGrantingTicket("casuser"),
            "https://cas.example.org", "https://other.example.org", artifactResolve);
        ticketRegistry.addTicket(ticket);

        val xml = SamlUtils.transformSamlObject(openSamlConfigBean, envelope).toString();
        val result = performSoapPost(xml);
        assertNotNull(result.getRequest().getAttribute(FaultString.class.getSimpleName()));
        assertNotNull(ticketRegistry.getTicket(ticket.getId()));
    }

    private MvcResult performSoapPost(final String xml) throws Exception {
        return mockMvc.perform(post(SamlIdPConstants.ENDPOINT_SAML1_SOAP_ARTIFACT_RESOLUTION)
            .contentType(MediaType.TEXT_XML)
            .content(xml.getBytes(StandardCharsets.UTF_8)))
            .andReturn();
    }

    private ArtifactResolve getArtifactResolve() {
        var builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(ArtifactResolve.DEFAULT_ELEMENT_NAME);
        val request = (ArtifactResolve) builder.buildObject();
        request.setID('_' + UUID.randomUUID().toString());
        request.setIssueInstant(Instant.now(Clock.systemUTC()));
        request.setDestination("http://localhost" + SamlIdPConstants.ENDPOINT_SAML1_SOAP_ARTIFACT_RESOLUTION);
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

    private ArtifactResolve getSignedArtifactResolve() throws Exception {
        val request = getArtifactResolve();
        return signSamlObject(new MockHttpServletRequest(), new MockHttpServletResponse(),
            request, samlRegisteredService, SAMLConstants.SAML2_SOAP11_BINDING_URI, request.getDestination());
    }
}
