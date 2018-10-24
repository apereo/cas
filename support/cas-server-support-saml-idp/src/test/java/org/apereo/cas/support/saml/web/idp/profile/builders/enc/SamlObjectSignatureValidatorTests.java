package org.apereo.cas.support.saml.web.idp.profile.builders.enc;

import org.apereo.cas.category.FileSystemCategory;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.InMemoryResourceMetadataResolver;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;

import lombok.val;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apache.commons.io.FileUtils;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.messaging.context.SAMLMetadataContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.common.messaging.context.SAMLSelfEntityContext;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.config.SAML2Configuration;
import org.pac4j.saml.context.SAML2MessageContext;
import org.pac4j.saml.sso.impl.SAML2AuthnRequestBuilder;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.File;

/**
 * This is {@link SamlObjectSignatureValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Category(FileSystemCategory.class)
public class SamlObjectSignatureValidatorTests extends BaseSamlIdPConfigurationTests {
    private SAML2Configuration saml2ClientConfiguration;
    private SAML2MessageContext saml2MessageContext;
    private MessageContext<SAMLObject> samlContext;
    private SamlRegisteredServiceServiceProviderMetadataFacade adaptor;

    @BeforeEach
    public void before() throws Exception {
        val idpMetadata = new File("src/test/resources/metadata/idp-metadata.xml").getCanonicalPath();
        val keystorePath = new File(FileUtils.getTempDirectory(), "keystore").getCanonicalPath();
        val spMetadataPath = new File(FileUtils.getTempDirectory(), "sp-metadata.xml").getCanonicalPath();

        saml2ClientConfiguration = new SAML2Configuration(keystorePath, "changeit", "changeit", idpMetadata);
        saml2ClientConfiguration.setServiceProviderEntityId("cas:example:sp");
        saml2ClientConfiguration.setServiceProviderMetadataPath(spMetadataPath);
        saml2ClientConfiguration.init();

        val saml2Client = new SAML2Client(saml2ClientConfiguration);
        saml2Client.setCallbackUrl("http://callback.example.org");
        saml2Client.init();

        samlContext = new MessageContext<>();
        saml2MessageContext = new SAML2MessageContext(samlContext);

        val peer = saml2MessageContext.getSubcontext(SAMLPeerEntityContext.class, true);
        peer.setEntityId("https://cas.example.org/idp");
        val md = peer.getSubcontext(SAMLMetadataContext.class, true);
        val idpResolver = SamlIdPUtils.getRoleDescriptorResolver(casSamlIdPMetadataResolver, true);
        md.setRoleDescriptor(idpResolver.resolveSingle(new CriteriaSet(
            new EntityIdCriterion(peer.getEntityId()), new EntityRoleCriterion(IDPSSODescriptor.DEFAULT_ELEMENT_NAME))));

        val self = saml2MessageContext.getSubcontext(SAMLSelfEntityContext.class, true);
        self.setEntityId(saml2ClientConfiguration.getServiceProviderEntityId());

        val sp = self.getSubcontext(SAMLMetadataContext.class, true);
        val spRes = new InMemoryResourceMetadataResolver(new File(spMetadataPath), openSamlConfigBean);
        spRes.setId(getClass().getSimpleName());
        spRes.initialize();
        val spResolver = SamlIdPUtils.getRoleDescriptorResolver(spRes, true);
        sp.setRoleDescriptor(spResolver.resolveSingle(new CriteriaSet(
            new EntityIdCriterion(self.getEntityId()), new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME))));

        val service = new SamlRegisteredService();
        service.setName("Sample");
        service.setServiceId(saml2ClientConfiguration.getServiceProviderEntityId());
        service.setId(100);
        service.setDescription("SAML Service");
        service.setMetadataLocation(spMetadataPath);

        adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId()).get();
    }

    @Test
    public void verifySamlAuthnRequestNotSigned() throws Exception {
        val request = new MockHttpServletRequest();
        val builder = new SAML2AuthnRequestBuilder(saml2ClientConfiguration);
        val authnRequest = builder.build(saml2MessageContext);
        samlObjectSignatureValidator.verifySamlProfileRequestIfNeeded(authnRequest, adaptor, request, samlContext);
    }
}
