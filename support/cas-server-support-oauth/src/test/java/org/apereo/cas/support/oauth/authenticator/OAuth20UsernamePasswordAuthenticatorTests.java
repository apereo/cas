package org.apereo.cas.support.oauth.authenticator;

import module java.base;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.http.HttpUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.profile.BasicUserProfile;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.jee.context.session.JEESessionStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
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
    private Authenticator authenticator;

    @Test
    void verifyAcceptedCredentialsWithClientId() {
        val credentials = new UsernamePasswordCredentials("casuser", "casuser");
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, "clientWithoutSecret");
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        authenticator.validate(new CallContext(ctx, new JEESessionStore()), credentials);
        assertNotNull(credentials.getUserProfile());
        assertEquals("casuser", credentials.getUserProfile().getId());
        assertFalse(((BasicUserProfile) credentials.getUserProfile()).getAuthenticationAttributes().isEmpty());
    }

    @Test
    void verifyAcceptedCredentialsWithClientSecret() {
        val credentials = new UsernamePasswordCredentials("casuser", "casuser");
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, "client");
        request.addParameter(OAuth20Constants.CLIENT_SECRET, "secret");
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        authenticator.validate(new CallContext(ctx, new JEESessionStore()), credentials);
        assertNotNull(credentials.getUserProfile());
        assertEquals("casuser", credentials.getUserProfile().getId());
        assertFalse(((BasicUserProfile) credentials.getUserProfile()).getAuthenticationAttributes().isEmpty());
    }

    @Test
    void verifyAcceptedCredentialsWithBadClientSecret() {
        val credentials = new UsernamePasswordCredentials("casuser", "casuser");
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, "client");
        request.addParameter(OAuth20Constants.CLIENT_SECRET, "secretnotfound");
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        assertThrows(CredentialsException.class,
            () -> authenticator.validate(new CallContext(ctx, new JEESessionStore()), credentials));
    }

    @Test
    void verifyAcceptedCredentialsWithServiceDisabled() {
        val credentials = new UsernamePasswordCredentials("casuser", "casuser");
        val request = new MockHttpServletRequest();

        val disabledService = new OAuthRegisteredService();
        disabledService.setName("OAuth");
        disabledService.setId(RandomUtils.nextLong());
        disabledService.setServiceId("https://www.example.org");
        disabledService.setClientSecret(UUID.randomUUID().toString());
        disabledService.setClientId(UUID.randomUUID().toString());
        disabledService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(false, false));
        servicesManager.save(disabledService);
        request.addParameter(OAuth20Constants.CLIENT_ID, disabledService.getClientId());
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        assertThrows(CredentialsException.class,
            () -> authenticator.validate(new CallContext(ctx, new JEESessionStore()), credentials));
    }

    @Test
    void verifyAcceptedCredentialsWithBadCredentials() {
        val credentials = new UsernamePasswordCredentials("casuser-something", "casuser");
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, "client");
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        assertThrows(CredentialsException.class,
            () -> authenticator.validate(new CallContext(ctx, new JEESessionStore()), credentials));
    }

    @Test
    void verifyAcceptedCredentialsWithoutClientSecret() {
        val credentials = new UsernamePasswordCredentials("casuser", "casuser");
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, "client");
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        assertThrows(CredentialsException.class,
            () -> authenticator.validate(new CallContext(ctx, new JEESessionStore()), credentials));
    }

    @Test
    void verifyAcceptedCredentialsWithoutClientId() {
        val credentials = new UsernamePasswordCredentials("casuser", "casuser");
        val request = new MockHttpServletRequest();
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        assertThrows(CredentialsException.class,
            () -> authenticator.validate(new CallContext(ctx, new JEESessionStore()), credentials));
    }

    @Test
    void verifyAcceptedCredentialsWithClientSecretWithBasicAuth() {
        val credentials = new UsernamePasswordCredentials("casuser", "casuser");
        val request = new MockHttpServletRequest();
        val headers = HttpUtils.createBasicAuthHeaders("client", "secret");
        val authz = headers.get(AUTHORIZATION);
        assertNotNull(authz);
        request.addHeader(AUTHORIZATION, authz);
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        authenticator.validate(new CallContext(ctx, new JEESessionStore()), credentials);
        assertNotNull(credentials.getUserProfile());
        assertEquals("casuser", credentials.getUserProfile().getId());
        assertFalse(((BasicUserProfile) credentials.getUserProfile()).getAuthenticationAttributes().isEmpty());
    }
}
