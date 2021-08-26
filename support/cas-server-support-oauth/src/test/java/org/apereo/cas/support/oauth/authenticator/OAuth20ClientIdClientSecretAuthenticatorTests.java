package org.apereo.cas.support.oauth.authenticator;

import org.apereo.cas.authentication.principal.NullPrincipal;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;

import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.JEESessionStore;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.exception.CredentialsException;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OAuth20ClientIdClientSecretAuthenticatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("OAuth")
public class OAuth20ClientIdClientSecretAuthenticatorTests {

    @TestConfiguration("NullPrincipalTestConfiguration")
    public static class NullPrincipalTestConfiguration {
        @Bean
        public PrincipalResolver defaultPrincipalResolver() {
            val mockPrincipalResolver = mock(PrincipalResolver.class);
            when(mockPrincipalResolver.resolve(any())).thenReturn(NullPrincipal.getInstance());
            return mockPrincipalResolver;
        }
    }

    @SuppressWarnings("ClassCanBeStatic")
    @Nested
    public class DefaultPrincipalResolutionTests extends BaseOAuth20AuthenticatorTests {
        @RetryingTest(3)
        public void verifyAuthentication() {
            val credentials = new UsernamePasswordCredentials("client", "secret");
            val request = new MockHttpServletRequest();
            val ctx = new JEEContext(request, new MockHttpServletResponse());
            oAuthClientAuthenticator.validate(credentials, ctx, JEESessionStore.INSTANCE);
            assertNotNull(credentials.getUserProfile());
            assertEquals("client", credentials.getUserProfile().getId());
        }

        @Test
        public void verifyAuthenticationWithGrantTypePassword() {
            val credentials = new UsernamePasswordCredentials("client", "secret");
            val request = new MockHttpServletRequest();
            val ctx = new JEEContext(request, new MockHttpServletResponse());
            request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PASSWORD.name());
            oAuthClientAuthenticator.validate(credentials, ctx, JEESessionStore.INSTANCE);
            assertNull(credentials.getUserProfile());
        }

        @Test
        public void verifyAuthenticationWithBadSecret() {
            val refreshToken = getRefreshToken(service);
            ticketRegistry.addTicket(refreshToken);

            val credentials = new UsernamePasswordCredentials(service.getClientId(), UUID.randomUUID().toString());
            val request = new MockHttpServletRequest();
            val ctx = new JEEContext(request, new MockHttpServletResponse());
            request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name());
            request.addParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());

            assertThrows(CredentialsException.class, () -> oAuthClientAuthenticator.validate(credentials, ctx, JEESessionStore.INSTANCE));
        }

        @Test
        public void verifyAuthenticationWithCodeChallengePkce() {
            val refreshToken = getRefreshToken(service);
            ticketRegistry.addTicket(refreshToken);

            val code = getCode();
            when(code.getCodeChallenge()).thenReturn(UUID.randomUUID().toString());
            ticketRegistry.addTicket(code);

            val credentials = new UsernamePasswordCredentials(service.getClientId(), service.getClientSecret());
            val request = new MockHttpServletRequest();
            val ctx = new JEEContext(request, new MockHttpServletResponse());
            request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name());
            request.addParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());
            request.addParameter(OAuth20Constants.CODE, code.getId());

            oAuthClientAuthenticator.validate(credentials, ctx, JEESessionStore.INSTANCE);
            assertNull(credentials.getUserProfile());
        }

        @Test
        public void verifyAuthenticationWithGrantTypeRefreshToken() {
            val refreshToken = getRefreshToken(serviceWithoutSecret);
            ticketRegistry.addTicket(refreshToken);

            val credentials = new UsernamePasswordCredentials("serviceWithoutSecret", refreshToken.getId());
            val service = new OAuthRegisteredService();
            service.setClientId(credentials.getUsername());
            servicesManager.save(service);

            val request = new MockHttpServletRequest();
            val ctx = new JEEContext(request, new MockHttpServletResponse());

            request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name());
            request.addParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
            request.addParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());

            oAuthClientAuthenticator.validate(credentials, ctx, JEESessionStore.INSTANCE);
            assertNull(credentials.getUserProfile());

            request.removeAllParameters();
            request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name());
            request.addParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());
            oAuthClientAuthenticator.validate(credentials, ctx, JEESessionStore.INSTANCE);
            assertNotNull(credentials.getUserProfile());

            request.removeAllParameters();
            request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name());
            request.addParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
            request.addParameter(OAuth20Constants.CLIENT_SECRET, "serviceWithoutSecret");
            request.addParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());
            oAuthClientAuthenticator.validate(credentials, ctx, JEESessionStore.INSTANCE);
            assertNotNull(credentials.getUserProfile());
        }

        @Test
        public void verifyAuthenticationWithAttributesMapping() {
            val credentials = new UsernamePasswordCredentials(serviceWithAttributesMapping.getClientId(), "secret");
            val request = new MockHttpServletRequest();
            val ctx = new JEEContext(request, new MockHttpServletResponse());
            oAuthClientAuthenticator.validate(credentials, ctx, JEESessionStore.INSTANCE);
            assertNotNull(credentials.getUserProfile());
            
            assertEquals(credentials.getUsername().toLowerCase(), credentials.getUserProfile().getId());
            assertNotNull(credentials.getUserProfile().getAttribute("eduPersonAffiliation"));
            assertNull(credentials.getUserProfile().getAttribute("groupMembership"));
        }
    }

    @SuppressWarnings("ClassCanBeStatic")
    @Import(NullPrincipalTestConfiguration.class)
    @Nested
    public class NullPrincipalResolutionTests extends BaseOAuth20AuthenticatorTests {
        @Test
        public void verifyAuthenticationWithoutResolvedPrincipal() {
            val credentials = new UsernamePasswordCredentials("serviceWithAttributesMapping", "secret");

            val service = new OAuthRegisteredService();
            service.setClientId(credentials.getUsername());
            servicesManager.save(service);

            val request = new MockHttpServletRequest();
            val ctx = new JEEContext(request, new MockHttpServletResponse());
            oAuthClientAuthenticator.validate(credentials, ctx, JEESessionStore.INSTANCE);
            assertNotNull(credentials.getUserProfile());
            assertEquals(credentials.getUsername(), credentials.getUserProfile().getId());
        }
    }
}
