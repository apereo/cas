package org.apereo.cas.support.oauth.authenticator;

import module java.base;
import org.apereo.cas.OAuth20TestUtils;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.services.OAuthRegisteredServiceClientSecret;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.http.HttpUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

/**
 * This is {@link OAuth20UsernamePasswordAuthenticatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("OAuth")
class OAuth20UsernamePasswordAuthenticatorTests extends BaseOAuth20AuthenticatorTests {
    @Autowired
    @Qualifier("oauthUserAuthenticator")
    private OAuth20ClientCredentialsAuthenticator authenticator;

    @Test
    void verifyAcceptedCredentialsWithClientId() {
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, "clientWithoutSecret");
        val credentialsProfile = authenticator.authenticate(OAuth20TestUtils.newClientAuthenticationToken(request, "casuser", "casuser"));
        assertTrue(credentialsProfile.isPresent());
        assertEquals("casuser", credentialsProfile.orElseThrow().getId());
        assertFalse(credentialsProfile.orElseThrow().getAuthenticationAttributes().isEmpty());
    }

    @Test
    void verifyAcceptedCredentialsWithClientSecret() {
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, "client");
        request.addParameter(OAuth20Constants.CLIENT_SECRET, "secret");
        val credentialsProfile = authenticator.authenticate(OAuth20TestUtils.newClientAuthenticationToken(request, "casuser", "casuser"));
        assertTrue(credentialsProfile.isPresent());
        assertEquals("casuser", credentialsProfile.orElseThrow().getId());
        assertFalse(credentialsProfile.orElseThrow().getAuthenticationAttributes().isEmpty());
    }

    @Test
    void verifyAcceptedCredentialsWithBadClientSecret() {
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, "client");
        request.addParameter(OAuth20Constants.CLIENT_SECRET, "secretnotfound");
        assertThrows(BadCredentialsException.class,
            () -> authenticator.authenticate(OAuth20TestUtils.newClientAuthenticationToken(request, "casuser", "casuser")));
    }

    @Test
    void verifyAcceptedCredentialsWithServiceDisabled() {
        val request = new MockHttpServletRequest();

        val disabledService = new OAuthRegisteredService();
        disabledService.setName("OAuth");
        disabledService.setId(RandomUtils.nextLong());
        disabledService.setServiceId("https://www.example.org");
        disabledService.setClientSecrets(CollectionUtils.wrapList(OAuthRegisteredServiceClientSecret.withoutExpiration(UUID.randomUUID().toString())));
        disabledService.setClientId(UUID.randomUUID().toString());
        disabledService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(false, false));
        servicesManager.save(disabledService);
        request.addParameter(OAuth20Constants.CLIENT_ID, disabledService.getClientId());
        assertThrows(BadCredentialsException.class,
            () -> authenticator.authenticate(OAuth20TestUtils.newClientAuthenticationToken(request, "casuser", "casuser")));
    }

    @Test
    void verifyAcceptedCredentialsWithBadCredentials() {
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, "client");
        assertThrows(BadCredentialsException.class,
            () -> authenticator.authenticate(OAuth20TestUtils.newClientAuthenticationToken(request, "casuser-something", "casuser")));
    }

    @Test
    void verifyAcceptedCredentialsWithoutClientSecret() {
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, "client");
        assertThrows(BadCredentialsException.class,
            () -> authenticator.authenticate(OAuth20TestUtils.newClientAuthenticationToken(request, "casuser", "casuser")));
    }

    @Test
    void verifyAcceptedCredentialsWithoutClientId() {
        val request = new MockHttpServletRequest();
        assertThrows(BadCredentialsException.class,
            () -> authenticator.authenticate(OAuth20TestUtils.newClientAuthenticationToken(request, "casuser", "casuser")));
    }

    @Test
    void verifyAcceptedCredentialsWithClientSecretWithBasicAuth() {
        val request = new MockHttpServletRequest();
        val headers = HttpUtils.createBasicAuthHeaders("client", "secret");
        val authz = headers.get(AUTHORIZATION);
        assertNotNull(authz);
        request.addHeader(AUTHORIZATION, authz);
        val credentialsProfile = authenticator.authenticate(OAuth20TestUtils.newClientAuthenticationToken(request, "casuser", "casuser"));
        assertTrue(credentialsProfile.isPresent());
        assertEquals("casuser", credentialsProfile.orElseThrow().getId());
        assertFalse(credentialsProfile.orElseThrow().getAuthenticationAttributes().isEmpty());
    }
}
