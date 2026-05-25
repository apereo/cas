package org.apereo.cas.support.saml.web.idp.profile.query;

import module java.base;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataAdaptor;
import org.apereo.cas.ticket.query.SamlAttributeQueryTicketFactory;
import lombok.val;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.AttributeValue;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.soap.common.SOAPObjectBuilder;
import org.opensaml.soap.soap11.Body;
import org.opensaml.soap.soap11.Envelope;
import org.opensaml.soap.soap11.FaultString;
import org.opensaml.soap.soap11.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * This is {@link SamlIdPSaml2AttributeQueryProfileHandlerControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML2Web")
class SamlIdPSaml2AttributeQueryProfileHandlerControllerTests {

    @Nested
    @TestPropertySource(properties = "cas.authn.saml-idp.core.attribute-query-profile-enabled=false")
    class DisabledTests extends BaseSamlIdPConfigurationTests {
        @Test
        void verifyOperation() throws Exception {
            val result = performSoapPost("<Envelope/>");
            assertEquals(HttpStatus.SC_NOT_IMPLEMENTED, result.getResponse().getStatus());
        }

        private MvcResult performSoapPost(final String xml) throws Exception {
            return mockMvc.perform(post(SamlIdPConstants.ENDPOINT_SAML2_SOAP_ATTRIBUTE_QUERY)
                .contentType(MediaType.TEXT_XML)
                .content(xml))
                .andReturn();
        }
    }

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @TestPropertySource(properties = "cas.authn.saml-idp.core.attribute-query-profile-enabled=true")
    class DefaultTests extends BaseSamlIdPConfigurationTests {
        @Autowired
        @Qualifier("samlAttributeQueryTicketFactory")
        private SamlAttributeQueryTicketFactory samlAttributeQueryTicketFactory;

        private SamlRegisteredService samlRegisteredService;

        @BeforeEach
        void beforeEach() {
            this.samlRegisteredService = getSamlRegisteredServiceFor(false, false,
                false, "https://cassp.example.org");
            servicesManager.save(samlRegisteredService);
        }

        @Test
        void verifyUnknownService() throws Exception {
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
            val query = getAttributeQuery(NameIDType.TRANSIENT, "casuser-aq");
            query.getIssuer().setValue(UUID.randomUUID().toString());
            body.getUnknownXMLObjects().add(query);
            envelope.setBody(body);

            val ticketGrantingTicket = new MockTicketGrantingTicket("casuser",
                Map.of("cn", List.of("CAS"), "lastName", List.of("Apereo")),
                Map.of("event-type", List.of("saml1-attr-query")));
            val ticket = samlAttributeQueryTicketFactory.create(
                "casuser-aq", query, "https://cassp.example.org", ticketGrantingTicket);
            ticketRegistry.addTicket(ticket);

            val xml = SamlUtils.transformSamlObject(openSamlConfigBean, envelope).toString();
            ticket.markTicketExpired();
            val result = performSoapPost(xml);
            assertEquals(HttpStatus.SC_OK, result.getResponse().getStatus());
            assertNotNull(result.getRequest().getAttribute(SamlIdPConstants.REQUEST_ATTRIBUTE_ERROR));
            assertNotNull(result.getRequest().getAttribute(FaultString.class.getSimpleName()));
        }

        @Test
        void verifyTicketExpired() throws Exception {
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
            val query = getAttributeQuery(NameIDType.TRANSIENT, "casuser-aq");
            body.getUnknownXMLObjects().add(query);
            envelope.setBody(body);

            val ticketGrantingTicket = new MockTicketGrantingTicket("casuser",
                Map.of("cn", List.of("CAS"), "lastName", List.of("Apereo")),
                Map.of("event-type", List.of("saml1-attr-query")));
            val ticket = samlAttributeQueryTicketFactory.create(
                "casuser-aq", query, "https://cassp.example.org", ticketGrantingTicket);
            ticketRegistry.addTicket(ticket);

            val xml = SamlUtils.transformSamlObject(openSamlConfigBean, envelope).toString();
            ticket.markTicketExpired();
            val result = performSoapPost(xml);
            assertNotNull(result.getRequest().getAttribute(SamlIdPConstants.REQUEST_ATTRIBUTE_ERROR));
            assertNotNull(result.getRequest().getAttribute(FaultString.class.getSimpleName()));
            assertEquals(HttpStatus.SC_OK, result.getResponse().getStatus());
        }

        @Test
        void verifyEncryptedNameIDFails() throws Exception {
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
            val query = getAttributeQuery(NameIDType.ENCRYPTED, "casuser-aq");
            body.getUnknownXMLObjects().add(query);
            envelope.setBody(body);

            val ticket = samlAttributeQueryTicketFactory.create(
                "casuser-aq",
                query, "https://cassp.example.org",
                new MockTicketGrantingTicket("casuser",
                    Map.of("cn", List.of("CAS"), "lastName", List.of("Apereo")),
                    Map.of("event-type", List.of("saml1-attr-query"))));
            ticketRegistry.addTicket(ticket);

            val xml = SamlUtils.transformSamlObject(openSamlConfigBean, envelope).toString();
            val result = performSoapPost(xml);
            assertNotNull(result.getRequest().getAttribute(SamlIdPConstants.REQUEST_ATTRIBUTE_ERROR));
            assertNotNull(result.getRequest().getAttribute(FaultString.class.getSimpleName()));
            assertEquals(HttpStatus.SC_OK, result.getResponse().getStatus());
        }

        @Test
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
            val query = getAttributeQuery(NameIDType.TRANSIENT, UUID.randomUUID().toString());
            body.getUnknownXMLObjects().add(query);
            envelope.setBody(body);

            val ticket = samlAttributeQueryTicketFactory.create(query.getSubject().getNameID().getValue(),
                query, "https://cassp.example.org",
                new MockTicketGrantingTicket("casuser",
                    Map.of("cn", List.of("CAS"), "lastName", List.of("Apereo")),
                    Map.of("event-type", List.of("saml1-attr-query"))));
            ticketRegistry.addTicket(ticket);

            val xml = SamlUtils.transformSamlObject(openSamlConfigBean, envelope).toString();
            val result = performSoapPost(xml);
            assertEquals(HttpStatus.SC_OK, result.getResponse().getStatus());
        }

        @Test
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
            val query = getAttributeQuery(NameIDType.TRANSIENT, UUID.randomUUID().toString());
            body.getUnknownXMLObjects().add(query);
            envelope.setBody(body);

            val xml = SamlUtils.transformSamlObject(openSamlConfigBean, envelope).toString();
            val result = performSoapPost(xml);
            assertNotNull(result.getRequest().getAttribute(SamlIdPConstants.REQUEST_ATTRIBUTE_ERROR));
            assertNotNull(result.getRequest().getAttribute(FaultString.class.getSimpleName()));
            assertEquals(HttpStatus.SC_OK, result.getResponse().getStatus());
        }

        private MvcResult performSoapPost(final String xml) throws Exception {
            return mockMvc.perform(post(SamlIdPConstants.ENDPOINT_SAML2_SOAP_ATTRIBUTE_QUERY)
                .contentType(MediaType.TEXT_XML)
                .content(xml.getBytes(StandardCharsets.UTF_8)))
                .andReturn();
        }

        private AttributeQuery getAttributeQuery(final String nameIdFormat, final String nameIdValue) {
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
            val nameId = (NameID) builder.buildObject();
            nameId.setValue(nameIdValue);
            nameId.setFormat(nameIdFormat);

            builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
                .getBuilder(Subject.DEFAULT_ELEMENT_NAME);
            val subject = (Subject) builder.buildObject();

            if (nameIdFormat.equals(NameIDType.ENCRYPTED)) {
                val facade = SamlRegisteredServiceMetadataAdaptor.get(defaultSamlRegisteredServiceCachingMetadataResolver,
                    samlRegisteredService, samlRegisteredService.getServiceId()).orElseThrow();
                val encryptedId = samlIdPObjectEncrypter.encode(nameId, samlRegisteredService, facade);
                subject.setEncryptedID(encryptedId);
            } else {
                subject.setNameID(nameId);
            }
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
}
