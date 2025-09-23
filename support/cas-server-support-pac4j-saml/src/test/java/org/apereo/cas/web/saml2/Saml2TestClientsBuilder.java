package org.apereo.cas.web.saml2;

import org.apereo.cas.authentication.principal.ClientCustomPropertyConstants;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.delegation.DelegationAutoRedirectTypes;
import org.apereo.cas.support.pac4j.authentication.clients.ConfigurableDelegatedClient;
import org.apereo.cas.support.pac4j.authentication.clients.ConfigurableDelegatedClientBuilder;
import org.apereo.cas.util.RandomUtils;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.config.SAML2Configuration;
import java.io.File;
import java.util.List;

/**
 * This is {@link Saml2TestClientsBuilder}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
public final class Saml2TestClientsBuilder implements ConfigurableDelegatedClientBuilder {
    public static final String IDP_METADATA_PATH = "src/test/resources/idp-metadata.xml";

    @Override
    public List<ConfigurableDelegatedClient> build(final CasConfigurationProperties casProperties) throws Exception {

        val saml2Config = newSAML2Configuration(IDP_METADATA_PATH);
        val saml2Client = new SAML2Client(saml2Config);
        saml2Client.getCustomProperties().put(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_AUTO_REDIRECT_TYPE, DelegationAutoRedirectTypes.CLIENT);
        saml2Client.getCustomProperties().put(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_DISPLAY_NAME, "SAML2Client");
        saml2Client.setCallbackUrl("http://callback.example.org");
        saml2Client.setName("SAML2Client");

        val saml2PostConfig = newSAML2Configuration(IDP_METADATA_PATH);
        saml2Config.setAuthnRequestBindingType(SAMLConstants.SAML2_POST_BINDING_URI);
        val saml2PostClient = new SAML2Client(saml2PostConfig);
        saml2PostClient.setCallbackUrl("http://callback.example.org");
        saml2PostClient.setName("SAML2ClientPostBinding");

        val saml2RedirectLogoutConfig = newSAML2Configuration(IDP_METADATA_PATH);
        saml2RedirectLogoutConfig.setSpLogoutRequestBindingType("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect");
        val saml2RedirectLogoutClient = new SAML2Client(saml2RedirectLogoutConfig);
        saml2RedirectLogoutClient.setCallbackUrl("http://callback.example.org");
        saml2RedirectLogoutClient.setName("SAML2RedirectLogoutClient");

        return List.of(
            new ConfigurableDelegatedClient(saml2Client),
            new ConfigurableDelegatedClient(saml2PostClient),
            new ConfigurableDelegatedClient(saml2RedirectLogoutClient)
        );

    }

    public static SAML2Configuration newSAML2Configuration(final String idpMetadataPath) throws Exception {
        val idpMetadata = new File(idpMetadataPath).getCanonicalPath();
        val keystorePath = new File(FileUtils.getTempDirectory(), "keystore-" + RandomUtils.nextInt()).getCanonicalPath();
        FileUtils.deleteQuietly(new File(keystorePath));
        val spMetadataPath = new File(FileUtils.getTempDirectory(), "sp-metadata-%s.xml".formatted(RandomUtils.nextInt())).getCanonicalPath();
        FileUtils.deleteQuietly(new File(spMetadataPath));
        val saml2Config = new SAML2Configuration(keystorePath, "changeit", "changeit", idpMetadata);
        saml2Config.setForceKeystoreGeneration(true);
        saml2Config.setForceServiceProviderMetadataGeneration(true);
        saml2Config.setServiceProviderEntityId("cas:example:sp");
        saml2Config.setServiceProviderMetadataPath(spMetadataPath);
        saml2Config.setAuthnRequestBindingType("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST");
        saml2Config.init();
        return saml2Config;
    }
}
