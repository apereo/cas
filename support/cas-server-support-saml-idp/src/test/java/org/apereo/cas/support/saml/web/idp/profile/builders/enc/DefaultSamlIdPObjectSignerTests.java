package org.apereo.cas.support.saml.web.idp.profile.builders.enc;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlIdPTestUtils;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataAdaptor;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultSamlIdPObjectSignerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("SAML2")
@TestPropertySource(properties = "cas.authn.saml-idp.metadata.file-system.location=classpath:metadata/")
class DefaultSamlIdPObjectSignerTests extends BaseSamlIdPConfigurationTests {

    private static final List<String> RSA_SIGNATURE_ALGORITHMS = List.of(
        SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1,
        SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256,
        SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA384,
        SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA512);

    @Test
    void findsSigningCredential() throws Exception {
        val samlRegisteredService = getSamlRegisteredServiceFor(true, true, false, "https://cassp.example.org");
        samlRegisteredService.setId(1000);
        samlRegisteredService.setName("ObjectSignerTest");
        samlRegisteredService.setSigningCredentialFingerprint("4f095b7ce6a7f49112c334a488185d55278177f9");

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val adaptor = SamlRegisteredServiceMetadataAdaptor.get(samlRegisteredServiceCachingMetadataResolver, samlRegisteredService,
                samlRegisteredService.getServiceId()).orElseThrow();
        val authnRequest = SamlIdPTestUtils.getAuthnRequest(openSamlConfigBean, samlRegisteredService);
        val encodedRequest = samlIdPObjectSigner.encode(authnRequest, samlRegisteredService, adaptor, response, request,
            SAMLConstants.SAML2_POST_BINDING_URI, authnRequest, new MessageContext());
        assertNotNull(encodedRequest);

    }

    @Test
    void verifyServiceSigningAlgorithmsAllBlocked() {
        val samlRegisteredService = getSamlRegisteredServiceFor(true, true, false, "https://cassp.example.org");
        samlRegisteredService.setId(1001);
        samlRegisteredService.setName("ObjectSignerBlockedTest");
        samlRegisteredService.setSigningSignatureAlgorithms(List.of(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256));
        samlRegisteredService.setSigningSignatureBlackListedAlgorithms(RSA_SIGNATURE_ALGORITHMS);
        verifySigningRefused(samlIdPObjectSigner, samlRegisteredService);
    }

    @Test
    void verifyGlobalSigningAlgorithmsAllBlocked() {
        val properties = new CasConfigurationProperties();
        properties.getAuthn().getSamlIdp().getAlgs()
            .setOverrideSignatureAlgorithms(List.of(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256))
            .setOverrideBlockedSignatureSigningAlgorithms(RSA_SIGNATURE_ALGORITHMS);
        val signer = new DefaultSamlIdPObjectSigner(casSamlIdPMetadataResolver, properties, samlIdPMetadataLocator);

        val samlRegisteredService = getSamlRegisteredServiceFor(true, true, false, "https://cassp.example.org");
        samlRegisteredService.setId(1002);
        samlRegisteredService.setName("ObjectSignerGlobalBlockedTest");
        verifySigningRefused(signer, samlRegisteredService);
    }

    @Test
    void verifySigningKeyParsedOncePerKeyMaterial() throws Throwable {
        val generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        val signingKey = generator.generateKeyPair().getPrivate();
        val rotatedSigningKey = generator.generateKeyPair().getPrivate();

        val metadataLocator = mock(SamlIdPMetadataLocator.class);
        when(metadataLocator.resolveSigningKey(any()))
            .thenReturn(new ByteArrayResource(signingKey.getEncoded()))
            .thenReturn(new ByteArrayResource(signingKey.getEncoded()))
            .thenReturn(new ByteArrayResource(rotatedSigningKey.getEncoded()));
        val signer = new DefaultSamlIdPObjectSigner(mock(MetadataResolver.class), new CasConfigurationProperties(), metadataLocator);
        val registeredService = new SamlRegisteredService();

        val parsedKey = signer.getSigningPrivateKey(registeredService);
        assertArrayEquals(signingKey.getEncoded(), parsedKey.getEncoded());
        assertSame(parsedKey, signer.getSigningPrivateKey(registeredService));
        assertArrayEquals(rotatedSigningKey.getEncoded(), signer.getSigningPrivateKey(registeredService).getEncoded());
    }

    @Test
    void verifySigningCredentialResolverBuiltOnce() throws Throwable {
        val signer = new DefaultSamlIdPObjectSigner(mock(MetadataResolver.class),
            new CasConfigurationProperties(), mock(SamlIdPMetadataLocator.class));
        assertSame(signer.getSigningCredentialResolver(), signer.getSigningCredentialResolver());
    }

    private void verifySigningRefused(final SamlIdPObjectSigner signer, final SamlRegisteredService samlRegisteredService) {
        val adaptor = SamlRegisteredServiceMetadataAdaptor.get(samlRegisteredServiceCachingMetadataResolver, samlRegisteredService,
            samlRegisteredService.getServiceId()).orElseThrow();
        val authnRequest = SamlIdPTestUtils.getAuthnRequest(openSamlConfigBean, samlRegisteredService);
        assertThrows(SamlException.class, () -> signer.encode(authnRequest, samlRegisteredService, adaptor,
            new MockHttpServletResponse(), new MockHttpServletRequest(),
            SAMLConstants.SAML2_POST_BINDING_URI, authnRequest, new MessageContext()));
        assertFalse(authnRequest.isSigned());
    }
}
