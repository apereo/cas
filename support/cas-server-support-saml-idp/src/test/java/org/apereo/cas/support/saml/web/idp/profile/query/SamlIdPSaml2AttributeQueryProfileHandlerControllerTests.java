package org.apereo.cas.support.saml.web.idp.profile.query;

import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.ticket.query.SamlAttributeQueryTicketFactory;

import lombok.val;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.AttributeValue;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.soap.common.SOAPObjectBuilder;
import org.opensaml.soap.soap11.Body;
import org.opensaml.soap.soap11.Envelope;
import org.opensaml.soap.soap11.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlIdPSaml2AttributeQueryProfileHandlerControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestPropertySource(properties = "cas.authn.saml-idp.attribute-query-profile-enabled=true")
public class SamlIdPSaml2AttributeQueryProfileHandlerControllerTests extends BaseSamlIdPConfigurationTests {
    @Autowired
    @Qualifier("saml2AttributeQueryProfileHandlerController")
    private SamlIdPSaml2AttributeQueryProfileHandlerController controller;

    @Autowired
    @Qualifier("samlAttributeQueryTicketFactory")
    private SamlAttributeQueryTicketFactory samlAttributeQueryTicketFactory;

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
        val query = getAttributeQuery();
        body.getUnknownXMLObjects().add(query);
        envelope.setBody(body);

        val ticket = samlAttributeQueryTicketFactory.create(query.getSubject().getNameID().getValue(),
            query, "https://cassp.example.org",
            new MockTicketGrantingTicket("casuser",
                Map.of("cn", List.of("CAS"), "lastName", List.of("Apereo")),
                Map.of("event-type", List.of("saml1-attr-query"))));
        ticketRegistry.addTicket(ticket);

        val xml = SamlUtils.transformSamlObject(openSamlConfigBean, envelope).toString();
        request.setContent(xml.getBytes(StandardCharsets.UTF_8));

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
        val query = getAttributeQuery();
        body.getUnknownXMLObjects().add(query);
        envelope.setBody(body);

        val xml = SamlUtils.transformSamlObject(openSamlConfigBean, envelope).toString();
        request.setContent(xml.getBytes(StandardCharsets.UTF_8));

        controller.handlePostRequest(response, request);
        assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatus());
    }

    private AttributeQuery getAttributeQuery() {
        var builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(AttributeQuery.DEFAULT_ELEMENT_NAME);
        val query = (AttributeQuery) builder.buildObject();
        builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
        val issuer = (Issuer) builder.buildObject();
        issuer.setValue(samlRegisteredService.getServiceId());
        query.setIssuer(issuer);

        builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(NameID.DEFAULT_ELEMENT_NAME);
        val nameid = (NameID) builder.buildObject();
        nameid.setValue(UUID.randomUUID().toString());
        nameid.setFormat(NameID.TRANSIENT);

        builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(Subject.DEFAULT_ELEMENT_NAME);
        val subject = (Subject) builder.buildObject();
        subject.setNameID(nameid);
        query.setSubject(subject);

        builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(Attribute.DEFAULT_ELEMENT_NAME);
        val attr1 = (Attribute) builder.buildObject();
        attr1.setName("cn");

        val value1 = (AttributeValue) ((SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(AttributeValue.DEFAULT_ELEMENT_NAME)).buildObject();
        value1.setTextContent("CAS");
        attr1.getAttributeValues().add(value1);
        query.getAttributes().add(attr1);

        val attr2 = (Attribute) builder.buildObject();
        attr2.setName("event-type");

        val value2 = (AttributeValue) ((SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(AttributeValue.DEFAULT_ELEMENT_NAME)).buildObject();
        value2.setTextContent("saml1-attr-query");
        attr2.getAttributeValues().add(value2);

        query.getAttributes().add(attr2);
        return query;
    }
}
