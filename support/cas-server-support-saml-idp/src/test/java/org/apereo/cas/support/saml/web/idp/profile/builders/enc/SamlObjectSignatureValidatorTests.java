package org.apereo.cas.support.saml.web.idp.profile.builders.enc;

import module java.base;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataAdaptor;
import org.apereo.cas.support.saml.util.credential.BasicX509CredentialFactoryBean;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.validate.SamlObjectSignatureValidator;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.binding.security.impl.SAMLOutboundProtocolMessageSigningHandler;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.SignatureSigningParameters;
import org.opensaml.xmlsec.context.SecurityParametersContext;
import org.opensaml.xmlsec.keyinfo.impl.X509KeyInfoGeneratorFactory;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlObjectSignatureValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("SAML2")
@TestPropertySource(properties = {
    "cas.authn.saml-idp.algs.override-blocked-signature-signing-algorithms=http://www.w3.org/2001/04/xmldsig-more#md5",
    "cas.authn.saml-idp.algs.override-allowed-signature-signing-algorithms="
        + "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256,http://www.w3.org/2001/04/xmlenc#sha256"
})
class SamlObjectSignatureValidatorTests extends BaseSamlIdPConfigurationTests {
    private static final String SERVICE_PROVIDER_ENTITY_ID = "cas:example:sp";

    private static final String SERVICE_PROVIDER_ACS_URL = "http://callback.example.org";

    private BasicX509Credential signingCredential;

    private MessageContext samlContext;

    private SamlRegisteredServiceMetadataAdaptor adaptor;

    /**
     * Prepare a service provider whose metadata is found at the given location. A location that is not on the
     * classpath is written as metadata of a service provider that signs with the test signing key pair, so
     * authentication requests signed with that key pair can be verified against it.
     *
     * @param spMetadataPath the metadata location of the service provider
     * @param spEntityId     the entity id of the service provider
     * @throws Exception the exception
     */
    private void setupTestContextFor(final String spMetadataPath, final String spEntityId) throws Exception {
        val certificate = new File("src/test/resources/metadata/idp-signing.crt");
        val factory = new BasicX509CredentialFactoryBean();
        factory.setCertificateResources(List.of(new FileSystemResource(certificate)));
        factory.setPrivateKeyResource(new FileSystemResource(new File("src/test/resources/metadata/idp-signing.key")));
        signingCredential = factory.getObject();

        if (!spMetadataPath.startsWith("classpath:")) {
            val encodedCertificate = FileUtils.readFileToString(certificate, StandardCharsets.UTF_8)
                .replace("-----BEGIN CERTIFICATE-----", StringUtils.EMPTY)
                .replace("-----END CERTIFICATE-----", StringUtils.EMPTY)
                .strip();
            val metadata = """
            <?xml version="1.0" encoding="UTF-8"?>
            <md:EntityDescriptor xmlns:md="urn:oasis:names:tc:SAML:2.0:metadata" entityID="%s">
                <md:SPSSODescriptor AuthnRequestsSigned="true" protocolSupportEnumeration="urn:oasis:names:tc:SAML:2.0:protocol">
                    <md:KeyDescriptor use="signing">
                        <ds:KeyInfo xmlns:ds="http://www.w3.org/2000/09/xmldsig#">
                            <ds:X509Data><ds:X509Certificate>%s</ds:X509Certificate></ds:X509Data>
                        </ds:KeyInfo>
                    </md:KeyDescriptor>
                    <md:AssertionConsumerService Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST" Location="%s" index="0"/>
                </md:SPSSODescriptor>
            </md:EntityDescriptor>
            """.formatted(spEntityId, encodedCertificate, SERVICE_PROVIDER_ACS_URL);
            FileUtils.writeStringToFile(new File(spMetadataPath), metadata, StandardCharsets.UTF_8);
        }
        samlContext = new MessageContext();

        val service = new SamlRegisteredService();
        service.setName("Sample");
        service.setServiceId(spEntityId);
        service.setId(100);
        service.setDescription("SAML Service");
        service.setMetadataLocation(spMetadataPath);

        val facade = SamlRegisteredServiceMetadataAdaptor.get(
            samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId());
        this.adaptor = facade.orElseThrow();
    }

    @Test
    void verifySamlAuthnRequestNotSigned() throws Throwable {
        val spMetadataPath = new File(FileUtils.getTempDirectory(), "sp-metadata-%s.xml".formatted(UUID.randomUUID())).getCanonicalPath();
        setupTestContextFor(spMetadataPath, SERVICE_PROVIDER_ENTITY_ID);
        val request = new MockHttpServletRequest();
        val authnRequest = buildAuthnRequest(SERVICE_PROVIDER_ENTITY_ID);
        assertThrows(SamlException.class,
            () -> samlObjectSignatureValidator.verifySamlProfileRequest(authnRequest, adaptor, request, samlContext));
    }

    @Test
    void verifySamlAuthnRequestSigned() throws Throwable {
        val spMetadataPath = new File(FileUtils.getTempDirectory(), "sp-metadata-%s.xml".formatted(UUID.randomUUID())).getCanonicalPath();
        setupTestContextFor(spMetadataPath, SERVICE_PROVIDER_ENTITY_ID);

        val request = new MockHttpServletRequest();
        val authnRequest = buildSignedAuthnRequest();

        assertDoesNotThrow(() -> samlObjectSignatureValidator.verifySamlProfileRequest(authnRequest, adaptor, request, samlContext));
        assertTrue(samlContext.ensureSubcontext(SAMLPeerEntityContext.class).isAuthenticated());
    }

    @Test
    void verifySamlAuthnRequestSignatureRejectsTamperedRequest() throws Throwable {
        val spMetadataPath = new File(FileUtils.getTempDirectory(), "sp-metadata-%s.xml".formatted(UUID.randomUUID())).getCanonicalPath();
        setupTestContextFor(spMetadataPath, SERVICE_PROVIDER_ENTITY_ID);

        val authnRequest = buildSignedAuthnRequest();
        authnRequest.setAssertionConsumerServiceURL("https://unregistered.example.org/acs");
        val xml = SamlUtils.transformSamlObject(openSamlConfigBean, authnRequest).toString();
        val tamperedRequest = Objects.requireNonNull(
            SamlUtils.transformSamlObject(openSamlConfigBean, xml, AuthnRequest.class));
        assertTrue(tamperedRequest.isSigned());

        val request = new MockHttpServletRequest();
        assertThrows(SamlException.class,
            () -> samlObjectSignatureValidator.verifySamlProfileRequest(tamperedRequest, adaptor, request, samlContext));
        assertFalse(samlContext.ensureSubcontext(SAMLPeerEntityContext.class).isAuthenticated());
    }

    @Test
    void verifySamlAuthnRequestRejectsBlockedEmbeddedSignatureAlgorithm() throws Throwable {
        val spMetadataPath = new File(FileUtils.getTempDirectory(), "sp-metadata-%s.xml".formatted(UUID.randomUUID())).getCanonicalPath();
        setupTestContextFor(spMetadataPath, SERVICE_PROVIDER_ENTITY_ID);

        val authnRequest = buildSignedAuthnRequest();
        val signature = Objects.requireNonNull(authnRequest.getSignature());
        val signatureAlgorithm = Objects.requireNonNull(signature.getSignatureAlgorithm());
        val validator = new SamlObjectSignatureValidator(
            List.of(), List.of(), List.of(signatureAlgorithm), List.of(), casProperties);

        val request = new MockHttpServletRequest();
        assertThrows(SignatureException.class,
            () -> validator.verifySamlProfileRequest(authnRequest, adaptor, request, samlContext));
        assertFalse(samlContext.ensureSubcontext(SAMLPeerEntityContext.class).isAuthenticated());
    }

    @Test
    void verifySamlAuthnRequestWithoutSignatureMultipleCertificates() throws Throwable {
        setupTestContextFor("classpath:metadata/sp-metadata-multicerts.xml", "https://bard.zoom.us");

        val request = new MockHttpServletRequest();
        val authnRequest = buildAuthnRequest("https://bard.zoom.us");

        assertThrows(SamlException.class,
            () -> samlObjectSignatureValidator.verifySamlProfileRequest(authnRequest, adaptor, request, samlContext));
    }

    @Test
    void verifySamlAuthnRequestWithoutSig() throws Throwable {
        val spMetadataPath = new File(FileUtils.getTempDirectory(), "sp-metadata-%s.xml".formatted(UUID.randomUUID())).getCanonicalPath();
        setupTestContextFor(spMetadataPath, SERVICE_PROVIDER_ENTITY_ID);

        val request = new MockHttpServletRequest();
        val authnRequest = buildAuthnRequest(SERVICE_PROVIDER_ENTITY_ID);
        val messageContext = new MessageContext();
        messageContext.setMessage(authnRequest);
        messageContext.ensureSubcontext(SecurityParametersContext.class).setSignatureSigningParameters(buildSignatureSigningParameters());

        assertThrows(SamlException.class,
            () -> samlObjectSignatureValidator.verifySamlProfileRequest(authnRequest, adaptor, request, samlContext));

    }

    private AuthnRequest buildAuthnRequest(final String spEntityId) {
        val authnRequest = getAuthnRequestFor(spEntityId);
        authnRequest.setAssertionConsumerServiceURL(SERVICE_PROVIDER_ACS_URL);
        return authnRequest;
    }

    private SignatureSigningParameters buildSignatureSigningParameters() {
        val parameters = new SignatureSigningParameters();
        parameters.setSigningCredential(signingCredential);
        parameters.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256);
        parameters.setSignatureReferenceDigestMethod(SignatureConstants.ALGO_ID_DIGEST_SHA256);
        parameters.setSignatureCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        val keyInfoGeneratorFactory = new X509KeyInfoGeneratorFactory();
        keyInfoGeneratorFactory.setEmitEntityCertificate(true);
        parameters.setKeyInfoGenerator(keyInfoGeneratorFactory.newInstance());
        return parameters;
    }

    private AuthnRequest buildSignedAuthnRequest() throws Exception {
        val authnRequest = buildAuthnRequest(SERVICE_PROVIDER_ENTITY_ID);
        val messageContext = new MessageContext();
        messageContext.setMessage(authnRequest);
        messageContext.ensureSubcontext(SecurityParametersContext.class).setSignatureSigningParameters(buildSignatureSigningParameters());

        val handler = new SAMLOutboundProtocolMessageSigningHandler();
        handler.initialize();
        handler.invoke(messageContext);
        val xml = SamlUtils.transformSamlObject(openSamlConfigBean, authnRequest).toString();
        return Objects.requireNonNull(SamlUtils.transformSamlObject(openSamlConfigBean, xml, AuthnRequest.class));
    }
}
