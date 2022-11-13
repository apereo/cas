package org.apereo.cas.support.pac4j.authentication.clients;

import org.apereo.cas.authentication.principal.ClientCustomPropertyConstants;
import org.apereo.cas.configuration.model.support.delegation.DelegationAutoRedirectTypes;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.http.OkAction;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.credentials.OAuth20Credentials;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.config.SAML2Configuration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

/**
 * This is {@link DelegatedAuthenticationClientsTestConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@TestConfiguration(value = "DelegatedAuthenticationClientsTestConfiguration", proxyBeanMethods = false)
public class DelegatedAuthenticationClientsTestConfiguration {
    @Bean
    public Clients builtClients() throws Exception {
        val saml2Config = getSAML2Configuration();
        val saml2Client = new SAML2Client(saml2Config);
        saml2Client.getCustomProperties().put(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_AUTO_REDIRECT_TYPE, DelegationAutoRedirectTypes.CLIENT);
        saml2Client.setCallbackUrl("http://callback.example.org");
        saml2Client.init();

        val saml2PostConfig = getSAML2Configuration();
        saml2Config.setAuthnRequestBindingType(SAMLConstants.SAML2_POST_BINDING_URI);
        val saml2PostClient = new SAML2Client(saml2PostConfig);
        saml2PostClient.setCallbackUrl("http://callback.example.org");
        saml2PostClient.setName("SAML2ClientPostBinding");
        saml2PostClient.init();

        val casClient = new CasClient(new CasConfiguration("https://sso.example.org/cas/login"));
        casClient.setCallbackUrl("http://callback.example.org");
        casClient.getCustomProperties().put(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_AUTO_REDIRECT_TYPE, DelegationAutoRedirectTypes.SERVER);
        casClient.init();

        val oidcCfg = new OidcConfiguration();
        oidcCfg.setClientId("client_id");
        oidcCfg.setSecret("client_secret");
        oidcCfg.setDiscoveryURI("https://dev-425954.oktapreview.com/.well-known/openid-configuration");
        val oidcClient = new OidcClient(oidcCfg);
        oidcClient.setCallbackUrl("http://callback.example.org");
        oidcClient.init();

        val facebookClient = new FacebookClient() {
            @Override
            public Optional<Credentials> retrieveCredentials(final WebContext context, final SessionStore sessionStore) {
                return Optional.of(new OAuth20Credentials("fakeVerifier"));
            }
        };
        facebookClient.setProfileCreator((credentials, context, store) -> {
            val profile = new CommonProfile();
            profile.setClientName(facebookClient.getName());
            val id = context.getRequestAttribute(Credentials.class.getName()).orElse("casuser");
            profile.setId(id.toString());
            profile.addAttribute("uid", "casuser");
            profile.addAttribute("givenName", "ApereoCAS");
            profile.addAttribute("memberOf", "admin");
            return Optional.of(profile);
        });
        facebookClient.setName(FacebookClient.class.getSimpleName());

        val mockClientNoCredentials = mock(BaseClient.class);
        when(mockClientNoCredentials.getName()).thenReturn("MockClientNoCredentials");
        when(mockClientNoCredentials.getCredentials(any(), any())).thenThrow(new OkAction(StringUtils.EMPTY));
        when(mockClientNoCredentials.isInitialized()).thenReturn(true);

        val failingClient = mock(IndirectClient.class);
        when(failingClient.getName()).thenReturn("FailingIndirectClient");
        doThrow(new IllegalArgumentException("Unable to init")).when(failingClient).init();

        return new Clients("https://cas.login.com", List.of(saml2Client, casClient,
            facebookClient, oidcClient, mockClientNoCredentials, failingClient, saml2PostClient));
    }

    private static SAML2Configuration getSAML2Configuration() throws IOException {
        val idpMetadata = new File("src/test/resources/idp-metadata.xml").getCanonicalPath();
        val keystorePath = new File(FileUtils.getTempDirectory(), "keystore").getCanonicalPath();
        val spMetadataPath = new File(FileUtils.getTempDirectory(), "sp-metadata.xml").getCanonicalPath();
        val saml2Config = new SAML2Configuration(keystorePath, "changeit", "changeit", idpMetadata);
        saml2Config.setServiceProviderEntityId("cas:example:sp");
        saml2Config.setServiceProviderMetadataPath(spMetadataPath);
        saml2Config.setAuthnRequestBindingType("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST");
        saml2Config.init();
        return saml2Config;
    }
}
