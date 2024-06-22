package org.apereo.cas.support.pac4j.authentication.clients;

import org.apereo.cas.authentication.principal.ClientCustomPropertyConstants;
import org.apereo.cas.configuration.model.support.delegation.DelegationAutoRedirectTypes;
import lombok.val;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.credentials.SessionKeyCredentials;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.exception.http.AutomaticFormPostAction;
import org.pac4j.core.exception.http.OkAction;
import org.pac4j.core.logout.LogoutType;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.client.OAuth20Client;
import org.pac4j.oauth.credentials.OAuth20Credentials;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
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
    @ConditionalOnProperty(name = "cas.custom.properties.delegation-test.enabled", havingValue = "true", matchIfMissing = true)
    public ConfigurableDelegatedClientBuilder testDelegatedIdentityProvidersBuilder() throws Exception {
        return () -> {
            val casClient = new CasClient(new CasConfiguration("https://sso.example.org/cas/login"));
            casClient.setCallbackUrl("http://callback.example.org");
            casClient.getCustomProperties().put(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_AUTO_REDIRECT_TYPE, DelegationAutoRedirectTypes.SERVER);

            val oidcCfg = new OidcConfiguration();
            oidcCfg.setClientId("client_id");
            oidcCfg.setSecret("client_secret");
            oidcCfg.setDiscoveryURI("https://dev-425954.oktapreview.com/.well-known/openid-configuration");
            val oidcClient = new OidcClient(oidcCfg);
            oidcClient.setCallbackUrl("http://callback.example.org");

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

            val badCredentialsClient = mock(IndirectClient.class);
            when(badCredentialsClient.getName()).thenReturn("BadCredentialsClient");
            when(badCredentialsClient.getCredentials(any())).thenThrow(new TechnicalException("Client failed to validate credentials"));
            return List.of(
                new ConfigurableDelegatedClient(casClient),
                new ConfigurableDelegatedClient(facebookClient),
                new ConfigurableDelegatedClient(oidcClient),
                new ConfigurableDelegatedClient(logoutClient),
                new ConfigurableDelegatedClient(logoutPostClient),
                new ConfigurableDelegatedClient(failingClient),
                new ConfigurableDelegatedClient(badCredentialsClient)
            );
        };
    }
}
