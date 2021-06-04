package org.apereo.cas.support.oauth.authenticator;

import lombok.val;
import org.apereo.cas.authentication.principal.NullPrincipal;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.JEESessionStore;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.util.AopTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This is {@link OAuth20ClientIdClientSecretAuthenticatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("OAuth")
@DirtiesContext
public class OAuth20ClientIdClientSecretAuthenticatorTests {

    @SuppressWarnings("ClassCanBeStatic")
    @Nested
    public class PositiveTestCases extends BaseOAuth20AuthenticatorTests {
        @Autowired
        private Authenticator oAuthClientAuthenticator;

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
        public void verifyAuthenticationWithGrantTypeRefreshToken() {
            val refreshToken = getRefreshToken(serviceWithoutSecret);
            ticketRegistry.addTicket(refreshToken);

            val credentials = new UsernamePasswordCredentials("serviceWithoutSecret", refreshToken.getId());
            val request = new MockHttpServletRequest();
            val ctx = new JEEContext(request, new MockHttpServletResponse());

            request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name());
            request.addParameter(OAuth20Constants.CLIENT_ID, "serviceWithoutSecret");
            request.addParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());

            val oAuth20ClientIdClientSecretAuthenticator = (OAuth20ClientIdClientSecretAuthenticator) AopTestUtils.getTargetObject(oAuthClientAuthenticator);
            assertNotNull(oAuth20ClientIdClientSecretAuthenticator);
            assertFalse(oAuth20ClientIdClientSecretAuthenticator.canAuthenticate(ctx));
            oAuth20ClientIdClientSecretAuthenticator.validate(credentials, ctx, JEESessionStore.INSTANCE);
            assertNull(credentials.getUserProfile());

            request.removeAllParameters();
            request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name());
            request.addParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());
            assertTrue(oAuth20ClientIdClientSecretAuthenticator.canAuthenticate(ctx));

            request.removeAllParameters();
            request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name());
            request.addParameter(OAuth20Constants.CLIENT_ID, "serviceWithoutSecret");
            request.addParameter(OAuth20Constants.CLIENT_SECRET, "serviceWithoutSecret");
            request.addParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());
            assertTrue(oAuth20ClientIdClientSecretAuthenticator.canAuthenticate(ctx));
        }

        @Test
        public void verifyAuthenticationWithAttributesMapping() {
            val credentials = new UsernamePasswordCredentials("serviceWithAttributesMapping", "secret");
            val request = new MockHttpServletRequest();
            val ctx = new JEEContext(request, new MockHttpServletResponse());
            oAuthClientAuthenticator.validate(credentials, ctx, JEESessionStore.INSTANCE);
            assertNotNull(credentials.getUserProfile());
            assertEquals("servicewithattributesmapping", credentials.getUserProfile().getId());
            assertNotNull(credentials.getUserProfile().getAttribute("eduPersonAffiliation"));
            assertNull(credentials.getUserProfile().getAttribute("groupMembership"));
        }
    }

    @Import(TestConfigForNullPrincipalResolution.class)
    @SuppressWarnings("ClassCanBeStatic")
    @Nested
    public class NullPrincipalTests extends BaseOAuth20AuthenticatorTests {

        @Autowired
        private Authenticator oAuthClientAuthenticator;

        @Autowired
        @Qualifier("servicesManager")
        private ObjectProvider<ServicesManager> servicesManager;

        @Test
        public void verifyAuthenticationWithoutResolvedPrincipal() {
            val id = "serviceWithAttributesMapping";
            val credentials = new UsernamePasswordCredentials(id, "secret");

            val service = new OAuthRegisteredService();
            service.setClientId(id);
            servicesManager.getObject().save(service);

            val request = new MockHttpServletRequest();
            val ctx = new JEEContext(request, new MockHttpServletResponse());
            oAuthClientAuthenticator.validate(credentials, ctx, JEESessionStore.INSTANCE);
            assertNotNull(credentials.getUserProfile());
            assertEquals(id, credentials.getUserProfile().getId());
        }
    }

    @TestConfiguration("TestConfigForNullPrincipalResolution")
    public static class TestConfigForNullPrincipalResolution {
        @Bean
        public PrincipalResolver defaultPrincipalResolver() {
            PrincipalResolver mockPrincipalResolver = mock(PrincipalResolver.class);
            when(mockPrincipalResolver.resolve(any())).thenReturn(new NullPrincipal());
            return mockPrincipalResolver;
        }
    }
}
