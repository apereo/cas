package org.apereo.cas.support.saml.web.idp.profile.builders.response;

import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.web.idp.profile.builders.AuthenticatedAssertionContext;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.apache.xerces.xs.XSObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.xmlsec.encryption.support.EncryptionConstants;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.pac4j.core.context.JEEContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlProfileSaml2ResponseBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("SAML")
public class SamlProfileSaml2ResponseBuilderTests extends BaseSamlIdPConfigurationTests {
    @Test
    public void verifySamlResponseAllSigned() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val service = getSamlRegisteredServiceForTestShib(true, true);
        service.getAttributeValueTypes().put("permissions", XSObject.class.getSimpleName());
        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver,
            service, service.getServiceId()).get();

        val authnRequest = getAuthnRequestFor(service);
        val assertion = getAssertion();

        val samlResponse = buildResponse(request, response, service,
            adaptor, authnRequest, assertion, SAMLConstants.SAML2_POST_BINDING_URI);
        assertNotNull(samlResponse);
    }

    @Test
    public void verifySamlResponseWithIssuerEntityId() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val service = getSamlRegisteredServiceForTestShib(true, true);
        service.setIssuerEntityId("https://issuer.example.org");
        service.getAttributeValueTypes().put("permissions", XSObject.class.getSimpleName());
        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver,
            service, service.getServiceId()).get();

        val authnRequest = getAuthnRequestFor(service);
        val assertion = getAssertion();

        val samlResponse = buildResponse(request, response, service,
            adaptor, authnRequest, assertion, SAMLConstants.SAML2_POST_BINDING_URI);
        assertNotNull(samlResponse);
    }

    @Test
    public void verifySamlResponseWithAttributeQuery() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);
        val webContext = new JEEContext(request, response);
        samlIdPDistributedSessionStore.set(webContext, WebUtils.PARAMETER_TICKET_GRANTING_TICKET_ID, tgt.getId());

        val service = getSamlRegisteredServiceForTestShib(true, true);
        service.setIssuerEntityId("https://issuer.example.org");
        service.getAttributeValueTypes().put("permissions", XSObject.class.getSimpleName());
        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver,
            service, service.getServiceId()).get();

        val authnRequest = getAuthnRequestFor(service);
        val assertion = getAssertion();

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, response));
        val samlResponse = buildResponse(request, response, service,
            adaptor, authnRequest, assertion, SAMLConstants.SAML2_ARTIFACT_BINDING_URI);
        assertNotNull(samlResponse);
    }

    @Test
    public void verifySamlResponseAllSignedEncrypted() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val service = getSamlRegisteredServiceForTestShib(true, true, true);
        service.setRequiredNameIdFormat(NameID.ENCRYPTED);
        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver,
            service, service.getServiceId()).get();

        val authnRequest = getAuthnRequestFor(service);
        val assertion = getAssertion();

        val samlResponse = buildResponse(request, response, service,
            adaptor, authnRequest, assertion, SAMLConstants.SAML2_POST_BINDING_URI);
        assertNotNull(samlResponse);
        assertTrue(samlResponse.getAssertions().isEmpty());
        assertFalse(samlResponse.getEncryptedAssertions().isEmpty());
    }

    @Test
    public void verifySamlResponseAssertionSigned() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val service = getSamlRegisteredServiceForTestShib(false, true);
        service.setRequiredNameIdFormat(NameID.ENCRYPTED);
        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver,
            service, service.getServiceId()).get();

        val authnRequest = getAuthnRequestFor(service);
        val assertion = getAssertion();

        val samlResponse = buildResponse(request, response, service,
            adaptor, authnRequest, assertion, SAMLConstants.SAML2_POST_BINDING_URI);
        assertNotNull(samlResponse);
        val assertions = samlResponse.getAssertions();
        assertFalse(assertions.isEmpty());
        assertNull(assertions.get(0).getSubject().getNameID());
        assertNotNull(assertions.get(0).getSubject().getEncryptedID());
    }

    @Test
    public void verifySamlResponseResponseSigned() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val service = getSamlRegisteredServiceForTestShib(true, false);
        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver,
            service, service.getServiceId()).get();

        val authnRequest = getAuthnRequestFor(service);
        val assertion = getAssertion();

        val samlResponse = buildResponse(request, response, service,
            adaptor, authnRequest, assertion, SAMLConstants.SAML2_POST_BINDING_URI);
        assertNotNull(samlResponse);
    }

    @Test
    public void verifySamlResponseNothingSigned() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val service = getSamlRegisteredServiceForTestShib(false, false);
        val adaptor =
            SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver,
                service, service.getServiceId()).get();

        val authnRequest = getAuthnRequestFor(service);
        val assertion = getAssertion();

        val samlResponse = buildResponse(request, response, service,
            adaptor, authnRequest, assertion, SAMLConstants.SAML2_POST_BINDING_URI);
        assertNotNull(samlResponse);
    }

    @Test
    public void verifySamlResponseSha1SigningAndDigest() {
        val request = new MockHttpServletRequest();
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

        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver,
            service, service.getServiceId()).get();

        val authnRequest = getAuthnRequestFor(service);
        val assertion = getAssertion();

        val samlResponse = buildResponse(request, response, service,
            adaptor, authnRequest, assertion, SAMLConstants.SAML2_POST_BINDING_URI);
        assertNotNull(samlResponse);
        assertEquals(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1, samlResponse.getAssertions().get(0).getSignature().getSignatureAlgorithm());
        assertEquals(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1, samlResponse.getSignature().getSignatureAlgorithm());
    }

    @Test
    public void verifySamlResponseSha256SigningAndDigest() {
        val request = new MockHttpServletRequest();
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

        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver,
            service, service.getServiceId()).get();

        val authnRequest = getAuthnRequestFor(service);
        val assertion = getAssertion();

        val samlResponse = buildResponse(request, response, service,
            adaptor, authnRequest, assertion, SAMLConstants.SAML2_POST_BINDING_URI);
        assertNotNull(samlResponse);
        assertEquals(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256, samlResponse.getAssertions().get(0).getSignature().getSignatureAlgorithm());
        assertEquals(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256, samlResponse.getSignature().getSignatureAlgorithm());
    }

    @Test
    public void verifySamlResponseAllSignedEncryptedWithCBC() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val service = getSamlRegisteredServiceForTestShib(true, true, true);
        service.setEncryptionDataAlgorithms(CollectionUtils.wrapArrayList(
            EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128));
        service.setEncryptionKeyAlgorithms(CollectionUtils.wrapArrayList(
            EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP));

        service.setRequiredNameIdFormat(NameID.ENCRYPTED);
        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver,
            service, service.getServiceId()).get();

        val authnRequest = getAuthnRequestFor(service);
        val assertion = getAssertion();

        val samlResponse = buildResponse(request, response, service, adaptor,
            authnRequest, assertion, SAMLConstants.SAML2_POST_BINDING_URI);
        assertNotNull(samlResponse);
        assertTrue(samlResponse.getAssertions().isEmpty());
        assertFalse(samlResponse.getEncryptedAssertions().isEmpty());
        assertEquals(EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128,
            samlResponse.getEncryptedAssertions().get(0).getEncryptedData().getEncryptionMethod().getAlgorithm());
    }

    @Test
    public void verifySamlResponseAllSignedEncryptedWithGCM() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val service = getSamlRegisteredServiceForTestShib(true, true, true);
        service.setEncryptionDataAlgorithms(CollectionUtils.wrapArrayList(
            EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128_GCM));
        service.setEncryptionKeyAlgorithms(CollectionUtils.wrapArrayList(
            EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP));

        service.setRequiredNameIdFormat(NameID.ENCRYPTED);
        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver,
            service, service.getServiceId()).get();

        val authnRequest = getAuthnRequestFor(service);
        val assertion = getAssertion();

        val samlResponse = buildResponse(request, response, service, adaptor,
            authnRequest, assertion, SAMLConstants.SAML2_POST_BINDING_URI);
        assertNotNull(samlResponse);
        assertTrue(samlResponse.getAssertions().isEmpty());
        assertFalse(samlResponse.getEncryptedAssertions().isEmpty());
        assertEquals(EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128_GCM,
            samlResponse.getEncryptedAssertions().get(0).getEncryptedData().getEncryptionMethod().getAlgorithm());
    }

    @Test
    public void verifySamlResponseAllSignedEncryptedWithEncryptionOptional() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val service = getSamlRegisteredServiceForTestShib(true, true, true);
        service.setEncryptionDataAlgorithms(CollectionUtils.wrapArrayList("something"));
        service.setEncryptionKeyAlgorithms(CollectionUtils.wrapArrayList("something"));
        service.setEncryptionOptional(true);
        service.setRequiredNameIdFormat(NameID.ENCRYPTED);
        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver,
            service, service.getServiceId()).get();

        val authnRequest = getAuthnRequestFor(service);
        val assertion = getAssertion();

        val samlResponse = buildResponse(request, response, service, adaptor,
            authnRequest, assertion, SAMLConstants.SAML2_POST_BINDING_URI);
        assertNotNull(samlResponse);
        assertFalse(samlResponse.getAssertions().isEmpty());
        assertTrue(samlResponse.getEncryptedAssertions().isEmpty());
    }

    private Response buildResponse(final MockHttpServletRequest request,
                                   final MockHttpServletResponse response,
                                   final SamlRegisteredService service,
                                   final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                   final AuthnRequest authnRequest,
                                   final AuthenticatedAssertionContext assertion,
                                   final String binding) {
        return samlProfileSamlResponseBuilder.build(authnRequest, request, response,
            assertion, service, adaptor, binding, new MessageContext());
    }
}
