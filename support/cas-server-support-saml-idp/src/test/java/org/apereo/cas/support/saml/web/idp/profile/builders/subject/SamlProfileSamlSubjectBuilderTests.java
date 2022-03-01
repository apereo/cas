package org.apereo.cas.support.saml.web.idp.profile.builders.subject;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileBuilderContext;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.NameID;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlProfileSamlSubjectBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("SAML2")
public class SamlProfileSamlSubjectBuilderTests extends BaseSamlIdPConfigurationTests {

    @Test
    public void verifySubjectWithNoNameId() throws Exception {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val service = getSamlRegisteredServiceForTestShib(true, true);
        service.setSkewAllowance(1000);
        service.setSkipGeneratingAssertionNameId(true);
        service.setSkipGeneratingSubjectConfirmationNotOnOrAfter(true);
        service.setSkipGeneratingSubjectConfirmationNameId(true);
        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(
            samlRegisteredServiceCachingMetadataResolver,
            service, service.getServiceId()).get();

        val authnRequest = getAuthnRequestFor(service);
        val assertion = getAssertion();

        val buildContext = SamlProfileBuilderContext.builder()
            .samlRequest(authnRequest)
            .httpRequest(request)
            .httpResponse(response)
            .authenticatedAssertion(assertion)
            .registeredService(service)
            .adaptor(adaptor)
            .binding(SAMLConstants.SAML2_POST_BINDING_URI)
            .build();
        val result = samlProfileSamlSubjectBuilder.build(buildContext);
        assertNotNull(result);
    }

    @Test
    public void verifySubjectWithSkewedConfData() throws Exception {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val service = getSamlRegisteredServiceForTestShib(true, true);
        service.setSkewAllowance(1000);
        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(
            samlRegisteredServiceCachingMetadataResolver,
            service, service.getServiceId()).get();

        val authnRequest = getAuthnRequestFor(service);
        val assertion = getAssertion();

        val now = ZonedDateTime.now(ZoneOffset.UTC)
            .plusSeconds(service.getSkewAllowance()).toInstant().truncatedTo(ChronoUnit.SECONDS);

        val buildContext = SamlProfileBuilderContext.builder()
            .samlRequest(authnRequest)
            .httpRequest(request)
            .httpResponse(response)
            .authenticatedAssertion(assertion)
            .registeredService(service)
            .adaptor(adaptor)
            .binding(SAMLConstants.SAML2_POST_BINDING_URI)
            .build();
        val subject = samlProfileSamlSubjectBuilder.build(buildContext);
        assertNotNull(subject);

        val subjectData = subject.getSubjectConfirmations().get(0).getSubjectConfirmationData();
        assertEquals(now, subjectData.getNotOnOrAfter().truncatedTo(ChronoUnit.SECONDS));
    }

    @Test
    public void verifyEncryptedSubject() throws Exception {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val service = getSamlRegisteredServiceForTestShib(true, true);
        service.setSkipGeneratingSubjectConfirmationNameId(false);
        service.setSkipGeneratingSubjectConfirmationNotOnOrAfter(false);
        service.setSkewAllowance(0);

        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(
            samlRegisteredServiceCachingMetadataResolver,
            service, service.getServiceId()).get();

        val authnRequest = getAuthnRequestFor(service);
        val assertion = getAssertion();

        service.setRequiredNameIdFormat(NameID.ENCRYPTED);
        val buildContext = SamlProfileBuilderContext.builder()
            .samlRequest(authnRequest)
            .httpRequest(request)
            .httpResponse(response)
            .authenticatedAssertion(assertion)
            .registeredService(service)
            .adaptor(adaptor)
            .binding(SAMLConstants.SAML2_POST_BINDING_URI)
            .build();
        val subject = samlProfileSamlSubjectBuilder.build(buildContext);
        assertNotNull(subject);

        val subjectConfirmation = subject.getSubjectConfirmations().get(0);
        assertNotNull(subjectConfirmation.getEncryptedID());
        assertNull(subjectConfirmation.getNameID());
        assertNotNull(subject.getEncryptedID());
        assertNull(subject.getNameID());
    }
}
