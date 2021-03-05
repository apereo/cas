package org.apereo.cas.support.saml.web.idp.profile.builders.enc;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.InMemoryResourceMetadataResolver;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;

import lombok.val;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.binding.security.impl.SAMLOutboundProtocolMessageSigningHandler;
import org.opensaml.saml.common.messaging.context.SAMLMetadataContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.common.messaging.context.SAMLSelfEntityContext;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.xmlsec.context.SecurityParametersContext;
import org.pac4j.core.context.JEEContext;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.config.SAML2Configuration;
import org.pac4j.saml.context.SAML2MessageContext;
import org.pac4j.saml.crypto.DefaultSignatureSigningParametersProvider;
import org.pac4j.saml.sso.impl.SAML2AuthnRequestBuilder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlObjectSignatureValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("SAML")
@TestPropertySource(properties = {
    "cas.authn.saml-idp.algs.override-blocked-signature-signing-algorithms=http://www.w3.org/2001/04/xmldsig-more#md5",
    "cas.authn.saml-idp.algs.override-allowed-signature-signing-algorithms=http://www.w3.org/2001/04/xmldsig-more#hmac-md5"
})
public class SamlObjectSignatureValidatorTests extends BaseSamlIdPConfigurationTests {
    private SAML2Configuration saml2ClientConfiguration;

    private SAML2MessageContext saml2MessageContext;

    private MessageContext samlContext;

    private SamlRegisteredServiceServiceProviderMetadataFacade adaptor;

    private void setupTestContextFor(final String spMetadataPath, final String spEntityId) throws Exception {
        val idpMetadata = new File("src/test/resources/metadata/idp-metadata.xml").getCanonicalPath();
        val keystorePath = new File(FileUtils.getTempDirectory(), "keystore").getCanonicalPath();
        saml2ClientConfiguration = new SAML2Configuration(keystorePath, "changeit", "changeit", idpMetadata);
        saml2ClientConfiguration.setServiceProviderEntityId(spEntityId);
        saml2ClientConfiguration.setServiceProviderMetadataPath(spMetadataPath);
        saml2ClientConfiguration.init();

        val saml2Client = new SAML2Client(saml2ClientConfiguration);
        saml2Client.setCallbackUrl("http://callback.example.org");
        saml2Client.init();

        samlContext = new MessageContext();
        saml2MessageContext = new SAML2MessageContext();
        saml2MessageContext.setSaml2Configuration(saml2ClientConfiguration);
        saml2MessageContext.setWebContext(new JEEContext(new MockHttpServletRequest(), new MockHttpServletResponse()));
        val peer = saml2MessageContext.getMessageContext().getSubcontext(SAMLPeerEntityContext.class, true);
        assertNotNull(peer);

        peer.setEntityId("https://cas.example.org/idp");
        val md = peer.getSubcontext(SAMLMetadataContext.class, true);
        assertNotNull(md);
        val idpResolver = SamlIdPUtils.getRoleDescriptorResolver(casSamlIdPMetadataResolver, true);

        md.setRoleDescriptor(idpResolver.resolveSingle(new CriteriaSet(
            new EntityIdCriterion(Objects.requireNonNull(peer.getEntityId())),
            new EntityRoleCriterion(IDPSSODescriptor.DEFAULT_ELEMENT_NAME))));

        val self = saml2MessageContext.getMessageContext().getSubcontext(SAMLSelfEntityContext.class, true);
        assertNotNull(self);
        self.setEntityId(saml2ClientConfiguration.getServiceProviderEntityId());

        val sp = self.getSubcontext(SAMLMetadataContext.class, true);
        assertNotNull(sp);
        val spRes = new InMemoryResourceMetadataResolver(saml2ClientConfiguration.getServiceProviderMetadataResource(), openSamlConfigBean);
        spRes.setId(getClass().getSimpleName());
        spRes.initialize();
        val spResolver = SamlIdPUtils.getRoleDescriptorResolver(spRes, true);
        sp.setRoleDescriptor(spResolver.resolveSingle(new CriteriaSet(
            new EntityIdCriterion(Objects.requireNonNull(self.getEntityId())),
            new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME))));

        val service = new SamlRegisteredService();
        service.setName("Sample");
        service.setServiceId(saml2ClientConfiguration.getServiceProviderEntityId());
        service.setId(100);
        service.setDescription("SAML Service");
        service.setMetadataLocation(spMetadataPath);

        val facade = SamlRegisteredServiceServiceProviderMetadataFacade.get(
            samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId());
        this.adaptor = facade.get();
    }

    @Test
    public void verifySamlAuthnRequestNotSigned() throws Exception {
        val spMetadataPath = new File(FileUtils.getTempDirectory(), "sp-metadata.xml").getCanonicalPath();
        setupTestContextFor(spMetadataPath, "cas:example:sp");
        val request = new MockHttpServletRequest();
        val builder = new SAML2AuthnRequestBuilder();
        val authnRequest = builder.build(saml2MessageContext);
        samlObjectSignatureValidator.verifySamlProfileRequestIfNeeded(authnRequest, adaptor, request, samlContext);
    }

    @Test
    public void verifySamlAuthnRequestSigned() throws Exception {
        val spMetadataPath = new File(FileUtils.getTempDirectory(), "sp-metadata.xml").getCanonicalPath();
        setupTestContextFor(spMetadataPath, "cas:example:sp");

        val request = new MockHttpServletRequest();
        val builder = new SAML2AuthnRequestBuilder();
        val authnRequest = builder.build(saml2MessageContext);

        val messageContext = new MessageContext();
        messageContext.setMessage(authnRequest);
        val secContext = messageContext.getSubcontext(SecurityParametersContext.class, true);

        val provider = new DefaultSignatureSigningParametersProvider(saml2ClientConfiguration);
        Objects.requireNonNull(secContext).setSignatureSigningParameters(provider.build(adaptor.getSsoDescriptor()));

        val handler = new SAMLOutboundProtocolMessageSigningHandler();
        handler.initialize();
        handler.invoke(messageContext);

        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                samlObjectSignatureValidator.verifySamlProfileRequestIfNeeded(authnRequest, adaptor, request, samlContext);
            }
        });
    }

    @Test
    public void verifySamlAuthnRequestSignedMultipleCertificates() throws Exception {
        setupTestContextFor("classpath:metadata/sp-metadata-multicerts.xml", "https://bard.zoom.us");

        val request = new MockHttpServletRequest();
        val builder = new SAML2AuthnRequestBuilder();
        saml2ClientConfiguration.setAuthnRequestSigned(true);
        val authnRequest = builder.build(saml2MessageContext);

        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                samlObjectSignatureValidator.verifySamlProfileRequestIfNeeded(authnRequest, adaptor, request, samlContext);
            }
        });

    }

    @Test
    public void verifySamlAuthnRequestWithoutSig() throws Exception {
        val spMetadataPath = new File(FileUtils.getTempDirectory(), "sp-metadata.xml").getCanonicalPath();
        setupTestContextFor(spMetadataPath, "cas:example:sp");

        val request = new MockHttpServletRequest();
        val builder = new SAML2AuthnRequestBuilder();
        val authnRequest = builder.build(saml2MessageContext);
        val messageContext = new MessageContext();
        messageContext.setMessage(authnRequest);
        val secContext = messageContext.getSubcontext(SecurityParametersContext.class, true);

        val provider = new DefaultSignatureSigningParametersProvider(saml2ClientConfiguration);
        Objects.requireNonNull(secContext).setSignatureSigningParameters(provider.build(adaptor.getSsoDescriptor()));

        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                samlObjectSignatureValidator.verifySamlProfileRequestIfNeeded(authnRequest, adaptor, request, samlContext);
            }
        });

    }
}
