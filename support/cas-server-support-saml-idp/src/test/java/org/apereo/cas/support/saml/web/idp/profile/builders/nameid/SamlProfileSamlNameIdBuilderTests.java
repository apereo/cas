package org.apereo.cas.support.saml.web.idp.profile.builders.nameid;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SamlProfileSamlNameIdBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("SAML")
@SuppressWarnings("JavaUtilDate")
public class SamlProfileSamlNameIdBuilderTests extends BaseSamlIdPConfigurationTests {
    @Autowired
    @Qualifier("samlProfileSamlNameIdBuilder")
    private SamlProfileObjectBuilder<NameID> samlProfileSamlNameIdBuilder;

    @Test
    public void verifyNoSupportedFormats() {
        val authnRequest = mock(AuthnRequest.class);
        val issuer = mock(Issuer.class);
        when(issuer.getValue()).thenReturn("https://idp.example.org");
        when(authnRequest.getIssuer()).thenReturn(issuer);

        val policy = mock(NameIDPolicy.class);
        when(policy.getFormat()).thenReturn(NameID.EMAIL);
        when(authnRequest.getNameIDPolicy()).thenReturn(policy);

        val service = new SamlRegisteredService();
        service.setServiceId("entity-id");
        service.setNameIdQualifier("https://qualifier.example.org");
        service.setServiceProviderNameIdQualifier("https://sp-qualifier.example.org");
        service.setRequiredNameIdFormat(NameID.UNSPECIFIED);

        val facade = mock(SamlRegisteredServiceServiceProviderMetadataFacade.class);
        when(facade.getEntityId()).thenReturn(service.getServiceId());
        when(facade.getSupportedNameIdFormats()).thenReturn(new ArrayList<>(0));
        val result = samlProfileSamlNameIdBuilder.build(authnRequest, new MockHttpServletRequest(),
            new MockHttpServletResponse(),
            getAssertion(), service, facade,
            SAMLConstants.SAML2_POST_BINDING_URI, new MessageContext());
        assertNotNull(result);
    }

    @Test
    public void verifyUnknownSupportedFormats() {
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

        val facade = mock(SamlRegisteredServiceServiceProviderMetadataFacade.class);
        when(facade.getEntityId()).thenReturn(service.getServiceId());
        when(facade.getSupportedNameIdFormats()).thenReturn(new ArrayList<>(0));
        val result = samlProfileSamlNameIdBuilder.build(authnRequest, new MockHttpServletRequest(), new MockHttpServletResponse(),
            getAssertion(null, Map.of()), service, facade, SAMLConstants.SAML2_POST_BINDING_URI, new MessageContext());
        assertNull(result);
    }


    @Test
    public void verifyNameId() {
        verifyNameIdByFormat(NameID.EMAIL);
        verifyNameIdByFormat(NameID.TRANSIENT);
        verifyNameIdByFormat(NameID.PERSISTENT);
        verifyNameIdByFormat(NameID.ENTITY);
        verifyNameIdByFormat(NameID.X509_SUBJECT);
        verifyNameIdByFormat(NameID.WIN_DOMAIN_QUALIFIED);
        verifyNameIdByFormat(NameID.KERBEROS);
    }

    @Test
    public void verifyPersistedNameIdFormat() {
        val service = getSamlRegisteredServiceForTestShib();
        service.setRequiredNameIdFormat(NameID.PERSISTENT);

        val authnRequest = getAuthnRequestFor(service);

        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver,
            service, service.getServiceId()).get();
        val subject = samlProfileSamlSubjectBuilder.build(authnRequest, new MockHttpServletRequest(), new MockHttpServletResponse(),
            getAssertion(), service, adaptor, SAMLConstants.SAML2_POST_BINDING_URI, new MessageContext());
        assertNotNull(subject.getNameID());
        assertEquals(NameID.PERSISTENT, subject.getNameID().getFormat());
        assertEquals(adaptor.getEntityId(), subject.getNameID().getSPNameQualifier());
        assertEquals("https://cas.example.org/idp", subject.getNameID().getNameQualifier());
    }

    @Test
    public void verifyPersistedNameIdFormatWithServiceEntityIdOverride() {
        val service = getSamlRegisteredServiceForTestShib();
        service.setRequiredNameIdFormat(NameID.PERSISTENT);
        service.setIssuerEntityId(UUID.randomUUID().toString());

        val authnRequest = getAuthnRequestFor(service);

        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver,
            service, service.getServiceId()).get();

        val subject = samlProfileSamlSubjectBuilder.build(authnRequest, new MockHttpServletRequest(), new MockHttpServletResponse(),
            getAssertion(), service, adaptor, SAMLConstants.SAML2_POST_BINDING_URI, new MessageContext());
        assertNotNull(subject.getNameID());
        assertEquals(NameID.PERSISTENT, subject.getNameID().getFormat());
        assertEquals(adaptor.getEntityId(), subject.getNameID().getSPNameQualifier());
        assertEquals(service.getIssuerEntityId(), subject.getNameID().getNameQualifier());
    }


    @Test
    @SuppressWarnings("JavaUtilDate")
    public void verifyEncryptedNameIdFormat() {
        val service = getSamlRegisteredServiceForTestShib();
        service.setRequiredNameIdFormat(NameID.ENCRYPTED);
        service.setSkipGeneratingSubjectConfirmationNameId(false);

        val authnRequest = getAuthnRequestFor(service);

        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(this.samlRegisteredServiceCachingMetadataResolver,
            service, service.getServiceId()).get();

        val request = new MockHttpServletRequest();
        val subject = samlProfileSamlSubjectBuilder.build(authnRequest, request, new MockHttpServletResponse(),
            getAssertion(), service, adaptor, SAMLConstants.SAML2_POST_BINDING_URI, new MessageContext());
        assertNull(subject.getNameID());
        assertNotNull(subject.getEncryptedID());
        assertFalse(subject.getSubjectConfirmations().isEmpty());
        val subjectConfirmation = subject.getSubjectConfirmations().get(0);
        assertNotNull(subjectConfirmation.getEncryptedID());
        assertNull(subjectConfirmation.getNameID());
        assertNotNull(request.getAttribute(NameID.class.getName()));
    }

    @Test
    public void verifySkipTransient() {
        val authnRequest = mock(AuthnRequest.class);
        val issuer = mock(Issuer.class);
        when(issuer.getValue()).thenReturn("https://idp.example.org");
        when(authnRequest.getIssuer()).thenReturn(issuer);

        val policy = mock(NameIDPolicy.class);
        when(policy.getFormat()).thenReturn(NameID.TRANSIENT);
        when(authnRequest.getNameIDPolicy()).thenReturn(policy);

        val service = new SamlRegisteredService();
        service.setServiceId("entity-id");
        service.setSkipGeneratingTransientNameId(true);
        service.setRequiredNameIdFormat(NameID.TRANSIENT);
        val facade = mock(SamlRegisteredServiceServiceProviderMetadataFacade.class);
        when(facade.getEntityId()).thenReturn(service.getServiceId());
        when(facade.getSupportedNameIdFormats()).thenReturn(CollectionUtils.wrapList(NameID.TRANSIENT));
        val result = samlProfileSamlNameIdBuilder.build(authnRequest, new MockHttpServletRequest(), new MockHttpServletResponse(),
            getAssertion(), service, facade, SAMLConstants.SAML2_POST_BINDING_URI, new MessageContext());
        assertNotNull(result);
        assertEquals(NameID.TRANSIENT, result.getFormat());
        assertEquals("casuser", result.getValue());
    }

    private void verifyNameIdByFormat(final String format) {
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
        val facade = mock(SamlRegisteredServiceServiceProviderMetadataFacade.class);
        when(facade.getEntityId()).thenReturn(service.getServiceId());
        when(facade.getSupportedNameIdFormats()).thenReturn(CollectionUtils.wrapList(NameID.TRANSIENT, format));
        val result = samlProfileSamlNameIdBuilder.build(authnRequest, new MockHttpServletRequest(), new MockHttpServletResponse(),
            getAssertion(), service, facade, SAMLConstants.SAML2_POST_BINDING_URI, new MessageContext());
        assertNotNull(result);
        assertEquals(format, result.getFormat());
        if (format.equals(NameID.TRANSIENT)) {
            assertNotEquals("casuser", result.getValue());
        } else {
            assertEquals("casuser", result.getValue());
        }
    }
}
