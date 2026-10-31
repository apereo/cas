package org.apereo.cas.support.oauth.authenticator;

import module java.base;
import org.apereo.cas.OAuth20TestUtils;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.NullPrincipal;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.services.OAuthRegisteredServiceClientSecret;
import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.TestPropertySource;
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
            val request = new MockHttpServletRequest();
            var credentialsProfile = oauthClientAuthenticator.authenticate(OAuth20TestUtils.newClientAuthenticationToken(request, serviceWithAttributesMapping.getClientId(), "secret"));
            assertTrue(credentialsProfile.isPresent());

            assertEquals(serviceWithAttributesMapping.getClientId().toLowerCase(Locale.ENGLISH), credentialsProfile.orElseThrow().getId());
            assertNotNull(credentialsProfile.orElseThrow().getAttribute("eduPersonAffiliation"));
            assertNotNull(credentialsProfile.orElseThrow().getAttribute("groupMembership"));
        }
    }

    @Nested
    class DefaultPrincipalResolutionTests extends BaseOAuth20AuthenticatorTests {
        @RetryingTest(3)
        void verifyAuthentication() throws Throwable {
            val code = getCode();
            ticketRegistry.addTicket(code);
            
            val request = new MockHttpServletRequest();
            request.addParameter(OAuth20Constants.CODE, code.getId());
            var credentialsProfile = oauthClientAuthenticator.authenticate(OAuth20TestUtils.newClientAuthenticationToken(request, "client", "secret"));
            assertTrue(credentialsProfile.isPresent());
            assertEquals("client", credentialsProfile.orElseThrow().getId());
        }

        @Test
        void verifyAuthenticationWithServiceIdPattern() {
            val registeredService = new OAuthRegisteredService();
            registeredService.setClientId(UUID.randomUUID().toString());
            registeredService.setName(UUID.randomUUID().toString());
            registeredService.setServiceId("https://app.example.org.*|https://github.com/apereo/cas");
            registeredService.setClientSecrets(CollectionUtils.wrapList(OAuthRegisteredServiceClientSecret.withoutExpiration("secret")));
            servicesManager.save(registeredService);

            val request = new MockHttpServletRequest();
            request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.CLIENT_CREDENTIALS.name().toLowerCase(Locale.ENGLISH));
            var credentialsProfile = oauthClientAuthenticator.authenticate(OAuth20TestUtils.newClientAuthenticationToken(request, registeredService.getClientId(), "secret"));
            assertTrue(credentialsProfile.isPresent());
        }

        @Test
        void verifyAuthenticationWithGrantTypePassword() {
            val request = new MockHttpServletRequest();
            request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PASSWORD.name());
            var credentialsProfile = oauthClientAuthenticator.authenticate(OAuth20TestUtils.newClientAuthenticationToken(request, "client", "secret"));
            assertTrue(credentialsProfile.isEmpty());
        }

        @Test
        void verifyAuthenticationWithBadSecret() throws Throwable {
            val refreshToken = getRefreshToken(service);
            ticketRegistry.addTicket(refreshToken);

            val request = new MockHttpServletRequest();
            request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name());
            request.addParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());

            assertThrows(BadCredentialsException.class,
                () -> oauthClientAuthenticator.authenticate(OAuth20TestUtils.newClientAuthenticationToken(request, service.getClientId(), UUID.randomUUID().toString())));
        }

        @Test
        void verifyAuthenticationWithCodeChallengePkce() throws Throwable {
            val refreshToken = getRefreshToken(service);
            ticketRegistry.addTicket(refreshToken);

            val code = getCode();
            when(code.getCodeChallenge()).thenReturn(UUID.randomUUID().toString());
            ticketRegistry.addTicket(code);

            val request = new MockHttpServletRequest();
            request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name());
            request.addParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());
            request.addParameter(OAuth20Constants.CODE, code.getId());

            var credentialsProfile = oauthClientAuthenticator.authenticate(OAuth20TestUtils.newClientAuthenticationToken(request,
                service.getClientId(), service.getClientSecrets().getFirst().getValue()));
            assertTrue(credentialsProfile.isEmpty());
        }

        @Test
        void verifyAuthenticationWithGrantTypeRefreshToken() throws Throwable {
            val refreshToken = getRefreshToken(serviceWithoutSecret);
            ticketRegistry.addTicket(refreshToken);

            val registeredService = new OAuthRegisteredService();
            registeredService.setClientId(serviceWithoutSecret.getClientId());
            registeredService.setName(UUID.randomUUID().toString());
            registeredService.setServiceId(CoreAuthenticationTestUtils.CONST_TEST_URL);
            servicesManager.save(registeredService);

            val request = new MockHttpServletRequest();

            request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name());
            request.addParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
            request.addParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());

            var credentialsProfile = oauthClientAuthenticator.authenticate(OAuth20TestUtils.newClientAuthenticationToken(request, serviceWithoutSecret.getClientId(), refreshToken.getId()));
            assertTrue(credentialsProfile.isEmpty());

            request.removeAllParameters();
            request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name());
            request.addParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());
            credentialsProfile = oauthClientAuthenticator.authenticate(OAuth20TestUtils.newClientAuthenticationToken(request, serviceWithoutSecret.getClientId(), refreshToken.getId()));
            assertTrue(credentialsProfile.isPresent());

            request.removeAllParameters();
            request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name());
            request.addParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
            request.addParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());
            credentialsProfile = oauthClientAuthenticator.authenticate(OAuth20TestUtils.newClientAuthenticationToken(request, serviceWithoutSecret.getClientId(), refreshToken.getId()));
            assertTrue(credentialsProfile.isEmpty());
        }
    }

    @Import(NullPrincipalTestConfiguration.class)
    @Nested
    class NullPrincipalResolutionTests extends BaseOAuth20AuthenticatorTests {
        @Test
        void verifyAuthenticationWithoutResolvedPrincipal() {

            val registeredService = new OAuthRegisteredService();
            registeredService.setClientId("serviceWithAttributesMapping");
            registeredService.setName(UUID.randomUUID().toString());
            registeredService.setServiceId(CoreAuthenticationTestUtils.CONST_TEST_URL);
            servicesManager.save(registeredService);

            val request = new MockHttpServletRequest();
            var credentialsProfile = oauthClientAuthenticator.authenticate(OAuth20TestUtils.newClientAuthenticationToken(request, "serviceWithAttributesMapping", "secret"));
            assertTrue(credentialsProfile.isPresent());
            assertEquals("serviceWithAttributesMapping", credentialsProfile.orElseThrow().getId());
        }
    }
}
