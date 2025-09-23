package org.apereo.cas.support.pac4j.authentication.clients;

import org.apereo.cas.authentication.principal.ClientCustomPropertyConstants;
import org.apereo.cas.configuration.model.support.delegation.DelegationAutoRedirectTypes;
import lombok.val;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.credentials.SessionKeyCredentials;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.exception.http.AutomaticFormPostAction;
import org.pac4j.core.exception.http.OkAction;
import org.pac4j.core.logout.LogoutType;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.http.credentials.authenticator.IpRegexpAuthenticator;
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

    private static final String CALLBACK_URL = "https://callback.example.org";

    @Bean
    @ConditionalOnProperty(name = "cas.custom.properties.delegation-test.enabled", havingValue = "true", matchIfMissing = true)
    public ConfigurableDelegatedClientBuilder testDelegatedIdentityProvidersBuilder() {
        return casProperties -> {
            val casClient = new CasClient(new CasConfiguration("https://sso.example.org/cas/login"));
            casClient.setCallbackUrl(CALLBACK_URL);
            casClient.getCustomProperties().put(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_AUTO_REDIRECT_TYPE, DelegationAutoRedirectTypes.SERVER);
            
            val logoutClient = mock(IndirectClient.class);
            when(logoutClient.getName()).thenReturn("LogoutClient");
            val sessionKeyCredentials = new SessionKeyCredentials(LogoutType.BACK, UUID.randomUUID().toString());
            when(logoutClient.getCredentialsExtractor())
                .thenReturn(callContext -> Optional.of(sessionKeyCredentials));
            when(logoutClient.validateCredentials(any(), any())).thenReturn(Optional.of(sessionKeyCredentials));
            when(logoutClient.getCredentials(any())).thenReturn(Optional.of(sessionKeyCredentials));
            when(logoutClient.isInitialized()).thenReturn(true);
            when(logoutClient.processLogout(any(), any())).thenReturn(new OkAction("Hello!"));
            when(logoutClient.getCallbackUrl()).thenReturn(CALLBACK_URL);

            val logoutPostClient = mock(IndirectClient.class);
            when(logoutPostClient.getName()).thenReturn("AutomaticPostLogoutClient");
            when(logoutPostClient.getCredentialsExtractor()).thenReturn(callContext -> Optional.of(sessionKeyCredentials));
            when(logoutPostClient.validateCredentials(any(), any())).thenReturn(Optional.of(sessionKeyCredentials));
            when(logoutPostClient.getCredentials(any())).thenReturn(Optional.of(sessionKeyCredentials));
            when(logoutPostClient.isInitialized()).thenReturn(true);
            when(logoutPostClient.processLogout(any(), any()))
                .thenReturn(new AutomaticFormPostAction("http://localhost/logout", Map.of("key", "value"), null));
            when(logoutPostClient.getCallbackUrl()).thenReturn(CALLBACK_URL);

            val failingClient = mock(IndirectClient.class);
            when(failingClient.getName()).thenReturn("FailingIndirectClient");
            when(failingClient.getCallbackUrl()).thenReturn(CALLBACK_URL);
            doThrow(new IllegalArgumentException("Unable to init")).when(failingClient).init();

            val badCredentialsClient = mock(IndirectClient.class);
            when(badCredentialsClient.getCallbackUrl()).thenReturn(CALLBACK_URL);
            when(badCredentialsClient.getName()).thenReturn("BadCredentialsClient");
            when(badCredentialsClient.getCredentials(any())).thenThrow(new TechnicalException("Client failed to validate credentials"));

            val fakeCredentials = new TokenCredentials(UUID.randomUUID().toString());
            val fakeClient = new IndirectClient() {
                @Override
                protected void internalInit(final boolean force) {
                }
                
                @Override
                public Optional<Credentials> getCredentials(final CallContext callContext) {
                    return Optional.of(fakeCredentials);
                }

                @Override
                public Optional<Credentials> internalValidateCredentials(final CallContext ctx, final Credentials credentials) {
                    return Optional.of(fakeCredentials);
                }
            };
            fakeClient.setCredentialsExtractor(callContext -> Optional.of(fakeCredentials));
            fakeClient.setRedirectionActionBuilder(callContext -> Optional.of(new OkAction("http://localhost")));
            fakeClient.setAuthenticator(new IpRegexpAuthenticator());
            fakeClient.setCallbackUrl(CALLBACK_URL);
            fakeClient.setProfileCreator((callContext, store) -> {
                val profile = new CommonProfile();
                profile.setClientName(fakeClient.getName());
                val id = callContext.webContext().getRequestAttribute(Credentials.class.getName()).orElse("casuser");
                profile.setId(id.toString());
                profile.addAttribute("uid", "casuser");
                profile.addAttribute("givenName", "ApereoCAS");
                profile.addAttribute("memberOf", "admin");
                return Optional.of(profile);
            });
            fakeClient.setName("FakeClient");
            
            return List.of(
                new ConfigurableDelegatedClient(casClient),
                new ConfigurableDelegatedClient(logoutClient),
                new ConfigurableDelegatedClient(logoutPostClient),
                new ConfigurableDelegatedClient(failingClient),
                new ConfigurableDelegatedClient(fakeClient),
                new ConfigurableDelegatedClient(badCredentialsClient)
            );
        };
    }
}
