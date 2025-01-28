package org.apereo.cas.support.saml.web.idp.profile.builders.nameid;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataAdaptor;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileBuilderContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDPolicy;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SamlProfileSamlNameIdBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("SAMLResponse")
class SamlProfileSamlNameIdBuilderTests extends BaseSamlIdPConfigurationTests {
    @Autowired
    @Qualifier("samlProfileSamlNameIdBuilder")
    private SamlProfileObjectBuilder<SAMLObject> samlProfileSamlNameIdBuilder;

    @Test
    void verifyNoSupportedFormats() throws Throwable {
        val authnRequest = mock(AuthnRequest.class);
        val issuer = mock(Issuer.class);
        when(issuer.getValue()).thenReturn("https://idp.example.org");
        when(authnRequest.getIssuer()).thenReturn(issuer);

        val policy = mock(NameIDPolicy.class);
        when(policy.getFormat()).thenReturn(NameIDType.EMAIL);
        when(authnRequest.getNameIDPolicy()).thenReturn(policy);

        val service = new SamlRegisteredService();
        service.setServiceId("entity-id");
        service.setNameIdQualifier("https://qualifier.example.org");
        service.setServiceProviderNameIdQualifier("https://sp-qualifier.example.org");
        service.setRequiredNameIdFormat(NameIDType.UNSPECIFIED);

        val facade = mock(SamlRegisteredServiceMetadataAdaptor.class);
        when(facade.getEntityId()).thenReturn(service.getServiceId());
        when(facade.getSupportedNameIdFormats()).thenReturn(new ArrayList<>());

        val buildContext = SamlProfileBuilderContext.builder()
            .samlRequest(authnRequest)
            .httpRequest(new MockHttpServletRequest())
            .httpResponse(new MockHttpServletResponse())
            .authenticatedAssertion(Optional.of(getAssertion()))
            .registeredService(service)
            .adaptor(facade)
            .binding(SAMLConstants.SAML2_POST_BINDING_URI)
            .build();

        val result = samlProfileSamlNameIdBuilder.build(buildContext);
        assertNotNull(result);
    }

    @Test
    void verifyUnknownSupportedFormats() throws Throwable {
        val authnRequest = mock(AuthnRequest.class);
        val issuer = mock(Issuer.class);
        when(issuer.getValue()).thenReturn("https://idp.example.org");
        when(authnRequest.getIssuer()).thenReturn(issuer);

        val policy = mock(NameIDPolicy.class);
        when(policy.getFormat()).thenReturn("badformat");
        when(authnRequest.getNameIDPolicy()).thenReturn(policy);

        val service = new SamlRegisteredService();
        service.setServiceId("entity-id");
        service.setNameIdQualifier("https://qualifier.example.org");
        service.setServiceProviderNameIdQualifier("https://sp-qualifier.example.org");

        val facade = mock(SamlRegisteredServiceMetadataAdaptor.class);
        when(facade.getEntityId()).thenReturn(service.getServiceId());
        when(facade.getSupportedNameIdFormats()).thenReturn(new ArrayList<>());

        val buildContext = SamlProfileBuilderContext.builder()
            .samlRequest(authnRequest)
            .httpRequest(new MockHttpServletRequest())
            .httpResponse(new MockHttpServletResponse())
            .authenticatedAssertion(Optional.of(getAssertion(null, Map.of())))
            .registeredService(service)
            .adaptor(facade)
            .binding(SAMLConstants.SAML2_POST_BINDING_URI)
            .build();

        val result = samlProfileSamlNameIdBuilder.build(buildContext);
        assertNull(result);
    }


    @Test
    void verifyNameId() throws Throwable {
        verifyNameIdByFormat(NameIDType.EMAIL);
        verifyNameIdByFormat(NameIDType.TRANSIENT);
        verifyNameIdByFormat(NameIDType.PERSISTENT);
        verifyNameIdByFormat(NameIDType.ENTITY);
        verifyNameIdByFormat(NameIDType.X509_SUBJECT);
        verifyNameIdByFormat(NameIDType.WIN_DOMAIN_QUALIFIED);
        verifyNameIdByFormat(NameIDType.KERBEROS);
        verifyNameIdByFormat(NameIDType.UNSPECIFIED);
    }

    @Test
    void verifyPersistedNameIdFormat() throws Throwable {
        val service = getSamlRegisteredServiceForTestShib();
        service.setRequiredNameIdFormat(NameIDType.PERSISTENT);

        val authnRequest = getAuthnRequestFor(service);

        val adaptor = SamlRegisteredServiceMetadataAdaptor.get(samlRegisteredServiceCachingMetadataResolver,
            service, service.getServiceId()).orElseThrow();
        val buildContext = SamlProfileBuilderContext.builder()
            .samlRequest(authnRequest)
            .httpRequest(new MockHttpServletRequest())
            .httpResponse(new MockHttpServletResponse())
            .authenticatedAssertion(Optional.of(getAssertion()))
            .registeredService(service)
            .adaptor(adaptor)
            .binding(SAMLConstants.SAML2_POST_BINDING_URI)
            .build();

        val subject = samlProfileSamlSubjectBuilder.build(buildContext);
        assertNotNull(subject.getNameID());
        assertEquals(NameIDType.PERSISTENT, subject.getNameID().getFormat());
        assertEquals(adaptor.getEntityId(), subject.getNameID().getSPNameQualifier());
        assertEquals("https://cas.example.org/idp", subject.getNameID().getNameQualifier());
    }

    @Test
    void verifyAttributeQueryNameID() throws Throwable {
        val service = getSamlRegisteredServiceForTestShib();
        service.setRequiredNameIdFormat(NameIDType.PERSISTENT);

        val aqNameId = mock(NameID.class);
        val nameIdValue = UUID.randomUUID().toString();
        when(aqNameId.getValue()).thenReturn(nameIdValue);
        when(aqNameId.getFormat()).thenReturn(NameIDType.UNSPECIFIED);
        val qualifier = UUID.randomUUID().toString();
        when(aqNameId.getSPNameQualifier()).thenReturn(qualifier);
        when(aqNameId.getNameQualifier()).thenReturn(qualifier);

        val aqSubject = mock(Subject.class);
        when(aqSubject.getNameID()).thenReturn(aqNameId);
        val attributeQuery = mock(AttributeQuery.class);
        when(attributeQuery.getSubject()).thenReturn(aqSubject);

        val adaptor = SamlRegisteredServiceMetadataAdaptor.get(samlRegisteredServiceCachingMetadataResolver,
            service, service.getServiceId()).orElseThrow();
        val buildContext = SamlProfileBuilderContext.builder()
            .samlRequest(attributeQuery)
            .httpRequest(new MockHttpServletRequest())
            .httpResponse(new MockHttpServletResponse())
            .authenticatedAssertion(Optional.of(getAssertion()))
            .registeredService(service)
            .adaptor(adaptor)
            .binding(SAMLConstants.SAML2_POST_BINDING_URI)
            .build();
        val subject = samlProfileSamlSubjectBuilder.build(buildContext);
        assertNotNull(subject.getNameID());

        assertEquals(nameIdValue, subject.getNameID().getValue());
        assertEquals(NameIDType.UNSPECIFIED, subject.getNameID().getFormat());
        assertEquals(qualifier, subject.getNameID().getSPNameQualifier());
        assertEquals(qualifier, subject.getNameID().getNameQualifier());
    }

    @Test
    void verifyPersistedNameIdFormatWithServiceEntityIdOverride() throws Throwable {
        val service = getSamlRegisteredServiceForTestShib();
        service.setRequiredNameIdFormat(NameIDType.PERSISTENT);
        service.setIssuerEntityId(UUID.randomUUID().toString());

        val authnRequest = getAuthnRequestFor(service);

        val adaptor = SamlRegisteredServiceMetadataAdaptor.get(samlRegisteredServiceCachingMetadataResolver,
            service, service.getServiceId()).orElseThrow();

        val buildContext = SamlProfileBuilderContext.builder()
            .samlRequest(authnRequest)
            .httpRequest(new MockHttpServletRequest())
            .httpResponse(new MockHttpServletResponse())
            .authenticatedAssertion(Optional.of(getAssertion()))
            .registeredService(service)
            .adaptor(adaptor)
            .binding(SAMLConstants.SAML2_POST_BINDING_URI)
            .build();

        val subject = samlProfileSamlSubjectBuilder.build(buildContext);
        assertNotNull(subject.getNameID());
        assertEquals(NameIDType.PERSISTENT, subject.getNameID().getFormat());
        assertEquals(adaptor.getEntityId(), subject.getNameID().getSPNameQualifier());
        assertEquals(service.getIssuerEntityId(), subject.getNameID().getNameQualifier());
    }

    @Test
    void verifyNameIdFormatSkipQualifiers() throws Throwable {
        val service = getSamlRegisteredServiceForTestShib();
        service.setRequiredNameIdFormat(NameIDType.PERSISTENT);
        service.setIssuerEntityId(UUID.randomUUID().toString());
        service.setSkipGeneratingNameIdQualifier(true);
        service.setSkipGeneratingServiceProviderNameIdQualifier(true);

        val authnRequest = getAuthnRequestFor(service);
        val adaptor = SamlRegisteredServiceMetadataAdaptor.get(samlRegisteredServiceCachingMetadataResolver,
            service, service.getServiceId()).orElseThrow();

        val buildContext = SamlProfileBuilderContext.builder()
            .samlRequest(authnRequest)
            .httpRequest(new MockHttpServletRequest())
            .httpResponse(new MockHttpServletResponse())
            .authenticatedAssertion(Optional.of(getAssertion()))
            .registeredService(service)
            .adaptor(adaptor)
            .binding(SAMLConstants.SAML2_POST_BINDING_URI)
            .build();

        val subject = samlProfileSamlSubjectBuilder.build(buildContext);
        assertNotNull(subject.getNameID());
        assertEquals(NameIDType.PERSISTENT, subject.getNameID().getFormat());
        assertNull(subject.getNameID().getSPNameQualifier());
        assertNull(subject.getNameID().getNameQualifier());
    }


    @Test
    void verifyEncryptedNameIdFormat() throws Throwable {
        val service = getSamlRegisteredServiceForTestShib();
        service.setRequiredNameIdFormat(NameIDType.ENCRYPTED);
        service.setSkipGeneratingSubjectConfirmationNameId(false);

        val authnRequest = getAuthnRequestFor(service);

        val adaptor = SamlRegisteredServiceMetadataAdaptor.get(this.samlRegisteredServiceCachingMetadataResolver,
            service, service.getServiceId()).orElseThrow();

        val request = new MockHttpServletRequest();
        val buildContext = SamlProfileBuilderContext.builder()
            .samlRequest(authnRequest)
            .httpRequest(request)
            .httpResponse(new MockHttpServletResponse())
            .authenticatedAssertion(Optional.of(getAssertion()))
            .registeredService(service)
            .adaptor(adaptor)
            .binding(SAMLConstants.SAML2_POST_BINDING_URI)
            .build();
        val subject = samlProfileSamlSubjectBuilder.build(buildContext);
        assertNull(subject.getNameID());
        assertNotNull(subject.getEncryptedID());
        assertFalse(subject.getSubjectConfirmations().isEmpty());
        val subjectConfirmation = subject.getSubjectConfirmations().getFirst();
        assertNotNull(subjectConfirmation.getEncryptedID());
        assertNull(subjectConfirmation.getNameID());
        assertNotNull(request.getAttribute(NameID.class.getName()));
    }

    @Test
    void verifySkipTransient() throws Throwable {
        val authnRequest = mock(AuthnRequest.class);
        val issuer = mock(Issuer.class);
        when(issuer.getValue()).thenReturn("https://idp.example.org");
        when(authnRequest.getIssuer()).thenReturn(issuer);

        val policy = mock(NameIDPolicy.class);
        when(policy.getFormat()).thenReturn(NameIDType.TRANSIENT);
        when(authnRequest.getNameIDPolicy()).thenReturn(policy);

        val service = new SamlRegisteredService();
        service.setServiceId("entity-id");
        service.setSkipGeneratingTransientNameId(true);
        service.setRequiredNameIdFormat(NameIDType.TRANSIENT);
        val facade = mock(SamlRegisteredServiceMetadataAdaptor.class);
        when(facade.getEntityId()).thenReturn(service.getServiceId());
        when(facade.getSupportedNameIdFormats()).thenReturn(CollectionUtils.wrapList(NameIDType.TRANSIENT));

        val buildContext = SamlProfileBuilderContext.builder()
            .samlRequest(authnRequest)
            .httpRequest(new MockHttpServletRequest())
            .httpResponse(new MockHttpServletResponse())
            .authenticatedAssertion(Optional.of(getAssertion()))
            .registeredService(service)
            .adaptor(facade)
            .binding(SAMLConstants.SAML2_POST_BINDING_URI)
            .build();
        val result = (NameID) samlProfileSamlNameIdBuilder.build(buildContext);
        assertNotNull(result);
        assertEquals(NameIDType.TRANSIENT, result.getFormat());
        assertEquals("casuser", result.getValue());
    }

    private void verifyNameIdByFormat(final String format) throws Exception {
        val authnRequest = mock(AuthnRequest.class);
        val issuer = mock(Issuer.class);
        when(issuer.getValue()).thenReturn("https://idp.example.org");
        when(authnRequest.getIssuer()).thenReturn(issuer);

        val policy = mock(NameIDPolicy.class);
        when(policy.getFormat()).thenReturn(format);
        when(authnRequest.getNameIDPolicy()).thenReturn(policy);

        val service = new SamlRegisteredService();
        service.setServiceId("entity-id");
        service.setRequiredNameIdFormat(format);
        val facade = mock(SamlRegisteredServiceMetadataAdaptor.class);
        when(facade.getEntityId()).thenReturn(service.getServiceId());
        when(facade.getSupportedNameIdFormats()).thenReturn(CollectionUtils.wrapList(NameIDType.TRANSIENT, format));

        val buildContext = SamlProfileBuilderContext.builder()
            .samlRequest(authnRequest)
            .httpRequest(new MockHttpServletRequest())
            .httpResponse(new MockHttpServletResponse())
            .authenticatedAssertion(Optional.of(getAssertion()))
            .registeredService(service)
            .adaptor(facade)
            .binding(SAMLConstants.SAML2_POST_BINDING_URI)
            .build();
        val result = (NameID) samlProfileSamlNameIdBuilder.build(buildContext);
        assertNotNull(result);
        assertEquals(format, result.getFormat());
        if (format.equals(NameIDType.TRANSIENT)) {
            assertNotEquals("casuser", result.getValue());
        } else {
            assertEquals("casuser", result.getValue());
        }
    }
}
