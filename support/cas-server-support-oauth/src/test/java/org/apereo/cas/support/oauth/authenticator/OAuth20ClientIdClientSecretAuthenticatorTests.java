package org.apereo.cas.support.oauth.authenticator;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.NullPrincipal;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.jee.context.session.JEESessionStore;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import java.util.Locale;
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
class OAuth20ClientIdClientSecretAuthenticatorTests {

    @TestConfiguration(value = "NullPrincipalTestConfiguration", proxyBeanMethods = false)
    static class NullPrincipalTestConfiguration {
        @Bean
        public PrincipalResolver defaultPrincipalResolver() throws Throwable {
            val mockPrincipalResolver = mock(PrincipalResolver.class);
            when(mockPrincipalResolver.resolve(any())).thenReturn(NullPrincipal.getInstance());
            return mockPrincipalResolver;
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.attribute-repository.stub.attributes.eduPersonAffiliation=developer",
        "cas.authn.attribute-repository.stub.attributes.groupMembership=adopters"
    })
    class AttributeMappingTests extends BaseOAuth20AuthenticatorTests {
        @RetryingTest(2)
        void verifyAuthenticationWithAttributesMapping() {
            val credentials = new UsernamePasswordCredentials(serviceWithAttributesMapping.getClientId(), "secret");
            val request = new MockHttpServletRequest();
            val ctx = new JEEContext(request, new MockHttpServletResponse());
            oauthClientAuthenticator.validate(new CallContext(ctx, new JEESessionStore()), credentials);
            assertNotNull(credentials.getUserProfile());

            assertEquals(credentials.getUsername().toLowerCase(Locale.ENGLISH), credentials.getUserProfile().getId());
            assertNotNull(credentials.getUserProfile().getAttribute("eduPersonAffiliation"));
            assertNotNull(credentials.getUserProfile().getAttribute("groupMembership"));
        }
    }

    @Nested
    class DefaultPrincipalResolutionTests extends BaseOAuth20AuthenticatorTests {
        @RetryingTest(3)
        void verifyAuthentication() throws Throwable {
            val code = getCode();
            ticketRegistry.addTicket(code);
            
            val credentials = new UsernamePasswordCredentials("client", "secret");
            val request = new MockHttpServletRequest();
            request.addParameter(OAuth20Constants.CODE, code.getId());
            val ctx = new JEEContext(request, new MockHttpServletResponse());
            oauthClientAuthenticator.validate(new CallContext(ctx, new JEESessionStore()), credentials);
            assertNotNull(credentials.getUserProfile());
            assertEquals("client", credentials.getUserProfile().getId());
        }

        @Test
        void verifyAuthenticationWithGrantTypePassword() {
            val credentials = new UsernamePasswordCredentials("client", "secret");
            val request = new MockHttpServletRequest();
            val ctx = new JEEContext(request, new MockHttpServletResponse());
            request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PASSWORD.name());
            oauthClientAuthenticator.validate(new CallContext(ctx, new JEESessionStore()), credentials);
            assertNull(credentials.getUserProfile());
        }

        @Test
        void verifyAuthenticationWithBadSecret() throws Throwable {
            val refreshToken = getRefreshToken(service);
            ticketRegistry.addTicket(refreshToken);

            val credentials = new UsernamePasswordCredentials(service.getClientId(), UUID.randomUUID().toString());
            val request = new MockHttpServletRequest();
            val ctx = new JEEContext(request, new MockHttpServletResponse());
            request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name());
            request.addParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());

            assertThrows(CredentialsException.class,
                () -> oauthClientAuthenticator.validate(new CallContext(ctx, new JEESessionStore()), credentials));
        }

        @Test
        void verifyAuthenticationWithCodeChallengePkce() throws Throwable {
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

            oauthClientAuthenticator.validate(new CallContext(ctx, new JEESessionStore()), credentials);
            assertNull(credentials.getUserProfile());
        }

        @Test
        void verifyAuthenticationWithGrantTypeRefreshToken() throws Throwable {
            val refreshToken = getRefreshToken(serviceWithoutSecret);
            ticketRegistry.addTicket(refreshToken);

            val credentials = new UsernamePasswordCredentials(serviceWithoutSecret.getClientId(), refreshToken.getId());
            val registeredService = new OAuthRegisteredService();
            registeredService.setClientId(credentials.getUsername());
            registeredService.setName(UUID.randomUUID().toString());
            registeredService.setServiceId(CoreAuthenticationTestUtils.CONST_TEST_URL);
            servicesManager.save(registeredService);

            val request = new MockHttpServletRequest();
            val ctx = new JEEContext(request, new MockHttpServletResponse());

            request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name());
            request.addParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
            request.addParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());

            oauthClientAuthenticator.validate(new CallContext(ctx, new JEESessionStore()), credentials);
            assertNull(credentials.getUserProfile());

            request.removeAllParameters();
            request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name());
            request.addParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());
            oauthClientAuthenticator.validate(new CallContext(ctx, new JEESessionStore()), credentials);
            assertNotNull(credentials.getUserProfile());

            request.removeAllParameters();
            request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name());
            request.addParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
            request.addParameter(OAuth20Constants.CLIENT_SECRET, serviceWithoutSecret.getClientSecret());
            request.addParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());
            oauthClientAuthenticator.validate(new CallContext(ctx, new JEESessionStore()), credentials);
            assertNotNull(credentials.getUserProfile());
        }
    }

    @Import(NullPrincipalTestConfiguration.class)
    @Nested
    class NullPrincipalResolutionTests extends BaseOAuth20AuthenticatorTests {
        @Test
        void verifyAuthenticationWithoutResolvedPrincipal() {
            val credentials = new UsernamePasswordCredentials("serviceWithAttributesMapping", "secret");

            val registeredService = new OAuthRegisteredService();
            registeredService.setClientId(credentials.getUsername());
            registeredService.setName(UUID.randomUUID().toString());
            registeredService.setServiceId(CoreAuthenticationTestUtils.CONST_TEST_URL);
            servicesManager.save(registeredService);

            val request = new MockHttpServletRequest();
            val ctx = new JEEContext(request, new MockHttpServletResponse());
            oauthClientAuthenticator.validate(new CallContext(ctx, new JEESessionStore()), credentials);
            assertNotNull(credentials.getUserProfile());
            assertEquals(credentials.getUsername(), credentials.getUserProfile().getId());
        }
    }
}
