package org.apereo.cas.support.pac4j.authentication.clients;

import org.apereo.cas.authentication.principal.ClientCustomPropertyConstants;
import org.apereo.cas.configuration.model.support.delegation.DelegationAutoRedirectTypes;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviderFactory;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviders;
import org.apereo.cas.util.RandomUtils;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.credentials.SessionKeyCredentials;
import org.pac4j.core.exception.http.AutomaticFormPostAction;
import org.pac4j.core.exception.http.OkAction;
import org.pac4j.core.logout.LogoutType;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.client.OAuth20Client;
import org.pac4j.oauth.credentials.OAuth20Credentials;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.config.SAML2Configuration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
    public DelegatedIdentityProviders delegatedIdentityProviders(final Collection<DelegatedClientFactoryCustomizer> customizers) throws Exception {
        val saml2Config = getSAML2Configuration();
        val saml2Client = new SAML2Client(saml2Config);
        saml2Client.getCustomProperties().put(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_AUTO_REDIRECT_TYPE, DelegationAutoRedirectTypes.CLIENT);
        saml2Client.setCallbackUrl("http://callback.example.org");
        saml2Client.setName("SAML2Client");
        customizers.forEach(customizer -> customizer.customize(saml2Client));
        saml2Client.init();

        val saml2PostConfig = getSAML2Configuration();
        saml2Config.setAuthnRequestBindingType(SAMLConstants.SAML2_POST_BINDING_URI);
        val saml2PostClient = new SAML2Client(saml2PostConfig);
        saml2PostClient.setCallbackUrl("http://callback.example.org");
        saml2PostClient.setName("SAML2ClientPostBinding");
        customizers.forEach(customizer -> customizer.customize(saml2PostClient));
        saml2PostClient.init();

        val casClient = new CasClient(new CasConfiguration("https://sso.example.org/cas/login"));
        casClient.setCallbackUrl("http://callback.example.org");
        casClient.getCustomProperties().put(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_AUTO_REDIRECT_TYPE, DelegationAutoRedirectTypes.SERVER);
        customizers.forEach(customizer -> customizer.customize(casClient));
        casClient.init();

        val oidcCfg = new OidcConfiguration();
        oidcCfg.setClientId("client_id");
        oidcCfg.setSecret("client_secret");
        oidcCfg.setDiscoveryURI("https://dev-425954.oktapreview.com/.well-known/openid-configuration");
        val oidcClient = new OidcClient(oidcCfg);
        oidcClient.setCallbackUrl("http://callback.example.org");
        customizers.forEach(customizer -> customizer.customize(oidcClient));
        oidcClient.init();

        val fakeCredentials = new OAuth20Credentials("fakeVerifier");
        val facebookClient = new OAuth20Client() {

            @Override
            public Optional<Credentials> getCredentials(final CallContext callContext) {
                return Optional.of(fakeCredentials);
            }

            @Override
            public Optional<Credentials> internalValidateCredentials(final CallContext ctx, final Credentials credentials) {
                return Optional.of(fakeCredentials);
            }
        };
        facebookClient.setCredentialsExtractor(callContext -> Optional.of(fakeCredentials));
        facebookClient.getConfiguration().setWithState(false);
        facebookClient.setProfileCreator((callContext, store) -> {
            val profile = new CommonProfile();
            profile.setClientName(facebookClient.getName());
            val id = callContext.webContext().getRequestAttribute(Credentials.class.getName()).orElse("casuser");
            profile.setId(id.toString());
            profile.addAttribute("uid", "casuser");
            profile.addAttribute("givenName", "ApereoCAS");
            profile.addAttribute("memberOf", "admin");
            return Optional.of(profile);
        });
        facebookClient.setName(FacebookClient.class.getSimpleName());
        customizers.forEach(customizer -> customizer.customize(facebookClient));

        val logoutClient = mock(BaseClient.class);
        when(logoutClient.getName()).thenReturn("LogoutClient");
        val sessionKeyCredentials = new SessionKeyCredentials(LogoutType.BACK, UUID.randomUUID().toString());
        when(logoutClient.getCredentialsExtractor())
            .thenReturn(callContext -> Optional.of(sessionKeyCredentials));
        when(logoutClient.validateCredentials(any(), any())).thenReturn(Optional.of(sessionKeyCredentials));
        when(logoutClient.getCredentials(any())).thenReturn(Optional.of(sessionKeyCredentials));
        when(logoutClient.isInitialized()).thenReturn(true);
        when(logoutClient.processLogout(any(), any())).thenReturn(new OkAction("Hello!"));

        val logoutPostClient = mock(BaseClient.class);
        when(logoutPostClient.getName()).thenReturn("AutomaticPostLogoutClient");
        when(logoutPostClient.getCredentialsExtractor()).thenReturn(callContext -> Optional.of(sessionKeyCredentials));
        when(logoutPostClient.validateCredentials(any(), any())).thenReturn(Optional.of(sessionKeyCredentials));
        when(logoutPostClient.getCredentials(any())).thenReturn(Optional.of(sessionKeyCredentials));
        when(logoutPostClient.isInitialized()).thenReturn(true);
        when(logoutPostClient.processLogout(any(), any()))
            .thenReturn(new AutomaticFormPostAction("http://localhost/logout", Map.of("key", "value"), null));

        val failingClient = mock(IndirectClient.class);
        when(failingClient.getName()).thenReturn("FailingIndirectClient");
        doThrow(new IllegalArgumentException("Unable to init")).when(failingClient).init();
        customizers.forEach(customizer -> customizer.customize(failingClient));

        val clients = List.of(saml2Client, casClient, facebookClient,
            oidcClient, logoutClient, logoutPostClient, failingClient, saml2PostClient);
        return new RefreshableDelegatedIdentityProviders("https://cas.login.com", DelegatedIdentityProviderFactory.withClients(clients));
    }

    private static SAML2Configuration getSAML2Configuration() throws IOException {
        val idpMetadata = new File("src/test/resources/idp-metadata.xml").getCanonicalPath();
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
