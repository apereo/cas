package org.apereo.cas.support.saml.web.idp.profile.builders.nameid;

import org.apereo.cas.category.FileSystemCategory;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.jasig.cas.client.authentication.AttributePrincipalImpl;
import org.jasig.cas.client.validation.Assertion;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.Test;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDPolicy;
import org.opensaml.saml.saml2.core.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Date;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SamlProfileSamlNameIdBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Category(FileSystemCategory.class)
public class SamlProfileSamlNameIdBuilderTests extends BaseSamlIdPConfigurationTests {
    @Autowired
    @Qualifier("samlProfileSamlNameIdBuilder")
    private SamlProfileObjectBuilder<NameID> samlProfileSamlNameIdBuilder;

    @Autowired
    @Qualifier("samlProfileSamlSubjectBuilder")
    private SamlProfileObjectBuilder<Subject> samlProfileSamlSubjectBuilder;

    @Test
    public void verifyAction() {
        val authnRequest = mock(AuthnRequest.class);
        val issuer = mock(Issuer.class);
        when(issuer.getValue()).thenReturn("https://idp.example.org");
        when(authnRequest.getIssuer()).thenReturn(issuer);

        val policy = mock(NameIDPolicy.class);
        when(policy.getFormat()).thenReturn(NameID.EMAIL);
        when(authnRequest.getNameIDPolicy()).thenReturn(policy);

        val service = new SamlRegisteredService();
        service.setServiceId("entity-id");
        service.setRequiredNameIdFormat(NameID.EMAIL);
        val facade = mock(SamlRegisteredServiceServiceProviderMetadataFacade.class);
        when(facade.getEntityId()).thenReturn(service.getServiceId());
        val assertion = mock(Assertion.class);
        when(assertion.getPrincipal()).thenReturn(new AttributePrincipalImpl("casuser"));

        when(facade.getSupportedNameIdFormats()).thenReturn(CollectionUtils.wrapList(NameID.TRANSIENT, NameID.EMAIL));
        val result = samlProfileSamlNameIdBuilder.build(authnRequest, new MockHttpServletRequest(), new MockHttpServletResponse(),
            assertion, service, facade, SAMLConstants.SAML2_POST_BINDING_URI, mock(MessageContext.class));
        assertNotNull(result);
        assertEquals(NameID.EMAIL, result.getFormat());
        assertEquals("casuser", result.getValue());
    }

    @Test
    public void verifyEncryptedNameIdFormat() {
        val service = getSamlRegisteredServiceForTestShib();
        service.setRequiredNameIdFormat(NameID.ENCRYPTED);
        service.setSkipGeneratingSubjectConfirmationNameId(false);
        
        val authnRequest = getAuthnRequestFor(service);
        
        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(this.samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId()).get();

        val assertion = mock(Assertion.class);
        when(assertion.getPrincipal()).thenReturn(new AttributePrincipalImpl("casuser"));
        when(assertion.getValidFromDate()).thenReturn(new Date());

        val subject = samlProfileSamlSubjectBuilder.build(authnRequest, new MockHttpServletRequest(), new MockHttpServletResponse(),
            assertion, service, adaptor, SAMLConstants.SAML2_POST_BINDING_URI, mock(MessageContext.class));
        assertNull(subject.getNameID());
        assertNotNull(subject.getEncryptedID());
        assertFalse(subject.getSubjectConfirmations().isEmpty());
        val subjectConfirmation = subject.getSubjectConfirmations().get(0);
        assertNotNull(subjectConfirmation.getEncryptedID());
        assertNull(subjectConfirmation.getNameID());
    }
}
