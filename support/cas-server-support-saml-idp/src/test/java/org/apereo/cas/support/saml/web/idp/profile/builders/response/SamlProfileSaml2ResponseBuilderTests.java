package org.apereo.cas.support.saml.web.idp.profile.builders.response;

import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataAdaptor;
import org.apereo.cas.support.saml.web.idp.profile.builders.AuthenticatedAssertionContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileBuilderContext;
import org.apereo.cas.ticket.query.SamlAttributeQueryTicket;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.apache.xerces.xs.XSObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Extensions;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.RequesterID;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.xmlsec.encryption.support.EncryptionConstants;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.pac4j.jee.context.JEEContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SamlProfileSaml2ResponseBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("SAMLResponse")
@TestPropertySource(properties = {
    "cas.tgc.crypto.enabled=false",
    "cas.authn.saml-idp.core.attribute-query-profile-enabled=true"
})
@Execution(ExecutionMode.SAME_THREAD)
class SamlProfileSaml2ResponseBuilderTests extends BaseSamlIdPConfigurationTests {

    @Test
    void verifySamlResponseAllSigned() throws Throwable {
        val request = buildHttpRequest();
        val response = new MockHttpServletResponse();

        val service = getSamlRegisteredServiceForTestShib(true, true);
        service.getAttributeValueTypes().put("permissions", XSObject.class.getSimpleName());
        val adaptor = SamlRegisteredServiceMetadataAdaptor.get(samlRegisteredServiceCachingMetadataResolver,
            service, service.getServiceId()).orElseThrow();

        val authnRequest = getAuthnRequestFor(service);
        val assertion = getAssertion();

        request.setAttribute(AttributeQuery.class.getSimpleName(), mock(AttributeQuery.class));
        val samlResponse = buildResponse(request, response, service,
            adaptor, authnRequest, assertion, SAMLConstants.SAML2_POST_BINDING_URI);
        assertNotNull(samlResponse);
        assertNull(request.getAttribute(SamlAttributeQueryTicket.class.getName()));
    }

    @Test
    void verifySamlResponseWithIssuerEntityId() throws Throwable {
        val request = buildHttpRequest();
        val response = new MockHttpServletResponse();

        val service = getSamlRegisteredServiceForTestShib(true, true);
        service.setIssuerEntityId("https://issuer.example.org");
        service.getAttributeValueTypes().put("permissions", XSObject.class.getSimpleName());
        val adaptor = SamlRegisteredServiceMetadataAdaptor.get(samlRegisteredServiceCachingMetadataResolver,
            service, service.getServiceId()).orElseThrow();

        val authnRequest = getAuthnRequestFor(service);
        val assertion = getAssertion();

        val samlResponse = buildResponse(request, response, service,
            adaptor, authnRequest, assertion, SAMLConstants.SAML2_POST_BINDING_URI);
        assertNotNull(samlResponse);
    }

    @Test
    void verifySamlResponseWithAttributeQuery() throws Throwable {
        val request = buildHttpRequest();
        val response = new MockHttpServletResponse();

        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);
        val webContext = new JEEContext(request, response);
        samlIdPDistributedSessionStore.set(webContext, WebUtils.PARAMETER_TICKET_GRANTING_TICKET_ID, tgt.getId());

        val service = getSamlRegisteredServiceForTestShib(true, true);
        service.setIssuerEntityId("https://issuer.example.org");
        service.getAttributeValueTypes().put("permissions", XSObject.class.getSimpleName());
        val adaptor = SamlRegisteredServiceMetadataAdaptor.get(samlRegisteredServiceCachingMetadataResolver,
            service, service.getServiceId()).orElseThrow();

        val authnRequest = getAuthnRequestFor(service);
        val assertion = getAssertion();

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, response));
        val samlResponse = buildResponse(request, response, service,
            adaptor, authnRequest, assertion, SAMLConstants.SAML2_ARTIFACT_BINDING_URI);
        assertNotNull(samlResponse);
    }

    @Test
    void verifySamlResponseAllSignedEncrypted() throws Throwable {
        val request = buildHttpRequest();
        val response = new MockHttpServletResponse();

        val service = getSamlRegisteredServiceForTestShib(true, true, true);
        service.setRequiredNameIdFormat(NameIDType.ENCRYPTED);
        val adaptor = SamlRegisteredServiceMetadataAdaptor.get(samlRegisteredServiceCachingMetadataResolver,
            service, service.getServiceId()).orElseThrow();

        val authnRequest = getAuthnRequestFor(service);
        val assertion = getAssertion();

        val samlResponse = buildResponse(request, response, service,
            adaptor, authnRequest, assertion, SAMLConstants.SAML2_POST_BINDING_URI);
        assertNotNull(samlResponse);
        assertTrue(samlResponse.getAssertions().isEmpty());
        assertFalse(samlResponse.getEncryptedAssertions().isEmpty());
    }

    @Test
    void verifySamlResponseAssertionSigned() throws Throwable {
        val request = buildHttpRequest();
        val response = new MockHttpServletResponse();

        val service = getSamlRegisteredServiceForTestShib(false, true);
        service.setRequiredNameIdFormat(NameIDType.ENCRYPTED);
        val adaptor = SamlRegisteredServiceMetadataAdaptor.get(samlRegisteredServiceCachingMetadataResolver,
            service, service.getServiceId()).orElseThrow();

        val authnRequest = getAuthnRequestFor(service);
        val assertion = getAssertion();

        val samlResponse = buildResponse(request, response, service,
            adaptor, authnRequest, assertion, SAMLConstants.SAML2_POST_BINDING_URI);
        assertNotNull(samlResponse);
        val assertions = samlResponse.getAssertions();
        assertFalse(assertions.isEmpty());
        assertNull(assertions.getFirst().getSubject().getNameID());
        assertNotNull(assertions.getFirst().getSubject().getEncryptedID());
    }

    @Test
    void verifySamlResponseResponseSigned() throws Throwable {
        val request = buildHttpRequest();
        val response = new MockHttpServletResponse();

        val service = getSamlRegisteredServiceForTestShib(true, false);
        val adaptor = SamlRegisteredServiceMetadataAdaptor.get(samlRegisteredServiceCachingMetadataResolver,
            service, service.getServiceId()).orElseThrow();

        val authnRequest = getAuthnRequestFor(service);
        val assertion = getAssertion();

        val samlResponse = buildResponse(request, response, service,
            adaptor, authnRequest, assertion, SAMLConstants.SAML2_POST_BINDING_URI);
        assertNotNull(samlResponse);
    }

    @Test
    void verifySamlResponseNothingSigned() throws Throwable {
        val request = buildHttpRequest();
        val response = new MockHttpServletResponse();

        val service = getSamlRegisteredServiceForTestShib(false, false);
        val adaptor =
            SamlRegisteredServiceMetadataAdaptor.get(samlRegisteredServiceCachingMetadataResolver,
                service, service.getServiceId()).orElseThrow();

        val authnRequest = getAuthnRequestFor(service);
        val assertion = getAssertion();

        val samlResponse = buildResponse(request, response, service,
            adaptor, authnRequest, assertion, SAMLConstants.SAML2_POST_BINDING_URI);
        assertNotNull(samlResponse);
    }

    @Test
    void verifySamlResponseSha1SigningAndDigest() throws Throwable {
        val request = buildHttpRequest();
        val response = new MockHttpServletResponse();

        val service = getSamlRegisteredServiceForTestShib(true, true);
        service.setSigningSignatureAlgorithms(CollectionUtils.wrapArrayList(
            SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1,
            SignatureConstants.ALGO_ID_SIGNATURE_ECDSA_SHA1));
        service.setSigningSignatureReferenceDigestMethods(CollectionUtils.wrapArrayList(
            SignatureConstants.ALGO_ID_DIGEST_SHA1));

        service.setSigningSignatureWhiteListedAlgorithms(CollectionUtils.wrapArrayList(
            SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1,
            SignatureConstants.ALGO_ID_SIGNATURE_ECDSA_SHA1,
            SignatureConstants.ALGO_ID_DIGEST_SHA1));

        val adaptor = SamlRegisteredServiceMetadataAdaptor.get(samlRegisteredServiceCachingMetadataResolver,
            service, service.getServiceId()).orElseThrow();

        val authnRequest = getAuthnRequestFor(service);
        val assertion = getAssertion();

        val samlResponse = buildResponse(request, response, service,
            adaptor, authnRequest, assertion, SAMLConstants.SAML2_POST_BINDING_URI);
        assertNotNull(samlResponse);
        assertEquals(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1, samlResponse.getAssertions().getFirst().getSignature().getSignatureAlgorithm());
        assertEquals(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1, samlResponse.getSignature().getSignatureAlgorithm());
    }

    @Test
    void verifySamlResponseSha256SigningAndDigest() throws Throwable {
        val request = buildHttpRequest();
        val response = new MockHttpServletResponse();

        val service = getSamlRegisteredServiceForTestShib(true, true);
        service.setSigningSignatureAlgorithms(CollectionUtils.wrapArrayList(
            SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256,
            SignatureConstants.ALGO_ID_SIGNATURE_ECDSA_SHA256));
        service.setSigningSignatureReferenceDigestMethods(CollectionUtils.wrapArrayList(
            SignatureConstants.ALGO_ID_DIGEST_SHA256));

        service.setSigningSignatureWhiteListedAlgorithms(CollectionUtils.wrapArrayList(
            SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256,
            SignatureConstants.ALGO_ID_DIGEST_SHA256,
            SignatureConstants.ALGO_ID_SIGNATURE_ECDSA_SHA256));

        service.setSigningSignatureBlackListedAlgorithms(CollectionUtils.wrapArrayList(
            SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA512,
            SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA384,
            SignatureConstants.ALGO_ID_SIGNATURE_ECDSA_SHA512,
            SignatureConstants.ALGO_ID_SIGNATURE_ECDSA_SHA384));

        val adaptor = SamlRegisteredServiceMetadataAdaptor.get(samlRegisteredServiceCachingMetadataResolver,
            service, service.getServiceId()).orElseThrow();

        val authnRequest = getAuthnRequestFor(service);
        val assertion = getAssertion();

        val samlResponse = buildResponse(request, response, service,
            adaptor, authnRequest, assertion, SAMLConstants.SAML2_POST_BINDING_URI);
        assertNotNull(samlResponse);
        assertEquals(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256, samlResponse.getAssertions().getFirst().getSignature().getSignatureAlgorithm());
        assertEquals(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256, samlResponse.getSignature().getSignatureAlgorithm());
    }

    @Test
    void verifySamlResponseAllSignedEncryptedWithCBC() throws Throwable {
        val request = buildHttpRequest();
        val response = new MockHttpServletResponse();

        val service = getSamlRegisteredServiceForTestShib(true, true, true);
        service.setEncryptionDataAlgorithms(CollectionUtils.wrapArrayList(
            EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128));
        service.setEncryptionKeyAlgorithms(CollectionUtils.wrapArrayList(
            EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP));

        service.setRequiredNameIdFormat(NameIDType.ENCRYPTED);
        val adaptor = SamlRegisteredServiceMetadataAdaptor.get(samlRegisteredServiceCachingMetadataResolver,
            service, service.getServiceId()).orElseThrow();

        val authnRequest = getAuthnRequestFor(service);
        val assertion = getAssertion();

        val samlResponse = buildResponse(request, response, service, adaptor,
            authnRequest, assertion, SAMLConstants.SAML2_POST_BINDING_URI);
        assertNotNull(samlResponse);
        assertTrue(samlResponse.getAssertions().isEmpty());
        assertFalse(samlResponse.getEncryptedAssertions().isEmpty());
        assertEquals(EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128,
            samlResponse.getEncryptedAssertions().getFirst().getEncryptedData().getEncryptionMethod().getAlgorithm());
    }

    @Test
    void verifySamlResponseAllSignedEncryptedWithGCM() throws Throwable {
        val request = buildHttpRequest();
        val response = new MockHttpServletResponse();

        val service = getSamlRegisteredServiceForTestShib(true, true, true);
        service.setEncryptionDataAlgorithms(CollectionUtils.wrapArrayList(
            EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128_GCM));
        service.setEncryptionKeyAlgorithms(CollectionUtils.wrapArrayList(
            EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP));

        service.setRequiredNameIdFormat(NameIDType.ENCRYPTED);
        val adaptor = SamlRegisteredServiceMetadataAdaptor.get(samlRegisteredServiceCachingMetadataResolver,
            service, service.getServiceId()).orElseThrow();

        val authnRequest = getAuthnRequestFor(service);
        val assertion = getAssertion();

        val samlResponse = buildResponse(request, response, service, adaptor,
            authnRequest, assertion, SAMLConstants.SAML2_POST_BINDING_URI);
        assertNotNull(samlResponse);
        assertTrue(samlResponse.getAssertions().isEmpty());
        assertFalse(samlResponse.getEncryptedAssertions().isEmpty());
        assertEquals(EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128_GCM,
            samlResponse.getEncryptedAssertions().getFirst().getEncryptedData().getEncryptionMethod().getAlgorithm());
    }

    @Test
    void verifySamlResponseAllSignedEncryptedWithEncryptionOptional() throws Throwable {
        val request = buildHttpRequest();
        val response = new MockHttpServletResponse();

        val service = getSamlRegisteredServiceForTestShib(true, true, true);
        service.setEncryptionDataAlgorithms(CollectionUtils.wrapArrayList("something"));
        service.setEncryptionKeyAlgorithms(CollectionUtils.wrapArrayList("something"));
        service.setEncryptionOptional(true);
        service.setRequiredNameIdFormat(NameIDType.ENCRYPTED);
        val adaptor = SamlRegisteredServiceMetadataAdaptor.get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId()).orElseThrow();

        val authnRequest = getAuthnRequestFor(service);
        val assertion = getAssertion();

        val samlResponse = buildResponse(request, response, service, adaptor,
            authnRequest, assertion, SAMLConstants.SAML2_POST_BINDING_URI);
        assertNotNull(samlResponse);
        assertFalse(samlResponse.getAssertions().isEmpty());
        assertTrue(samlResponse.getEncryptedAssertions().isEmpty());
    }

    @Test
    void verifySamlResponseForUnsolicitedRequest() throws Throwable {
        val request = buildHttpRequest();
        val response = new MockHttpServletResponse();

        val service = getSamlRegisteredServiceForTestShib(false, false);
        val adaptor = SamlRegisteredServiceMetadataAdaptor.get(samlRegisteredServiceCachingMetadataResolver,
            service, service.getServiceId()).orElseThrow();

        val authnRequest = getAuthnRequestFor(service);
        val extensions = samlProfileSamlResponseBuilder.newSamlObject(Extensions.class);
        val requesterID = samlProfileSamlResponseBuilder.newSamlObject(RequesterID.class);
        requesterID.setURI(casProperties.getAuthn().getSamlIdp().getCore().getEntityId());
        extensions.getUnknownXMLObjects().add(requesterID);
        authnRequest.setExtensions(extensions);
        val assertion = getAssertion();
        val samlResponse = buildResponse(request, response, service,
            adaptor, authnRequest, assertion, SAMLConstants.SAML2_POST_BINDING_URI);
        assertNotNull(samlResponse);
        assertNull(samlResponse.getInResponseTo());
        val subject = samlResponse.getAssertions().getFirst().getSubject();
        val subjectConfirmation = subject.getSubjectConfirmations().getFirst();
        assertNull(subjectConfirmation.getSubjectConfirmationData().getInResponseTo());
    }

    private MockHttpServletRequest buildHttpRequest() throws Exception {
        val request = new MockHttpServletRequest();
        val tgt = new MockTicketGrantingTicket("casuser");
        request.addHeader(casProperties.getTgc().getName(), tgt.getId());
        ticketRegistry.addTicket(tgt);
        return request;
    }

    private Response buildResponse(final MockHttpServletRequest request,
                                   final MockHttpServletResponse response,
                                   final SamlRegisteredService registeredService,
                                   final SamlRegisteredServiceMetadataAdaptor adaptor,
                                   final AuthnRequest authnRequest,
                                   final AuthenticatedAssertionContext assertion,
                                   final String binding) throws Exception {
        val buildContext = SamlProfileBuilderContext.builder()
            .samlRequest(authnRequest)
            .httpRequest(request)
            .httpResponse(response)
            .authenticatedAssertion(Optional.of(assertion))
            .registeredService(registeredService)
            .adaptor(adaptor)
            .binding(binding)
            .build();
        return samlProfileSamlResponseBuilder.build(buildContext);
    }
}
