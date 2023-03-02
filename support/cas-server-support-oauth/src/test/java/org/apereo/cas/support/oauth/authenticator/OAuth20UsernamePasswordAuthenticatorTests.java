package org.apereo.cas.support.oauth.authenticator;

import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.util.HttpUtils;

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
import static org.springframework.http.HttpHeaders.*;

/**
 * This is {@link OAuth20UsernamePasswordAuthenticatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("OAuth")
public class OAuth20UsernamePasswordAuthenticatorTests extends BaseOAuth20AuthenticatorTests {
    @Autowired
    @Qualifier("oauthUserAuthenticator")
    private Authenticator authenticator;

    @Test
    public void verifyAcceptedCredentialsWithClientId() {
        val credentials = new UsernamePasswordCredentials("casuser", "casuser");
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, "clientWithoutSecret");
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        authenticator.validate(new CallContext(ctx, JEESessionStore.INSTANCE), credentials);
        assertNotNull(credentials.getUserProfile());
        assertEquals("casuser", credentials.getUserProfile().getId());
        assertTrue(((BasicUserProfile) credentials.getUserProfile()).getAuthenticationAttributes().size() >= 1);
    }

    @Test
    public void verifyAcceptedCredentialsWithClientSecret() {
        val credentials = new UsernamePasswordCredentials("casuser", "casuser");
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, "client");
        request.addParameter(OAuth20Constants.CLIENT_SECRET, "secret");
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        authenticator.validate(new CallContext(ctx, JEESessionStore.INSTANCE), credentials);
        assertNotNull(credentials.getUserProfile());
        assertEquals("casuser", credentials.getUserProfile().getId());
        assertTrue(((BasicUserProfile) credentials.getUserProfile()).getAuthenticationAttributes().size() >= 1);
    }

    @Test
    public void verifyAcceptedCredentialsWithBadClientSecret() {
        val credentials = new UsernamePasswordCredentials("casuser", "casuser");
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, "client");
        request.addParameter(OAuth20Constants.CLIENT_SECRET, "secretnotfound");
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        assertThrows(CredentialsException.class,
            () -> authenticator.validate(new CallContext(ctx, JEESessionStore.INSTANCE), credentials));
    }

    @Test
    public void verifyAcceptedCredentialsWithServiceDisabled() {
        val credentials = new UsernamePasswordCredentials("casuser", "casuser");
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, "client");
        service.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(false, false));
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        assertThrows(CredentialsException.class,
            () -> authenticator.validate(new CallContext(ctx, JEESessionStore.INSTANCE), credentials));
    }

    @Test
    public void verifyAcceptedCredentialsWithBadCredentials() {
        val credentials = new UsernamePasswordCredentials("casuser-something", "casuser");
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, "client");
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        assertThrows(CredentialsException.class,
            () -> authenticator.validate(new CallContext(ctx, JEESessionStore.INSTANCE), credentials));
    }

    @Test
    public void verifyAcceptedCredentialsWithoutClientSecret() {
        val credentials = new UsernamePasswordCredentials("casuser", "casuser");
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, "client");
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        assertThrows(CredentialsException.class,
            () -> authenticator.validate(new CallContext(ctx, JEESessionStore.INSTANCE), credentials));
    }

    @Test
    public void verifyAcceptedCredentialsWithoutClientId() {
        val credentials = new UsernamePasswordCredentials("casuser", "casuser");
        val request = new MockHttpServletRequest();
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        assertThrows(CredentialsException.class,
            () -> authenticator.validate(new CallContext(ctx, JEESessionStore.INSTANCE), credentials));
    }

    @Test
    public void verifyAcceptedCredentialsWithClientSecretWithBasicAuth() {
        val credentials = new UsernamePasswordCredentials("casuser", "casuser");
        val request = new MockHttpServletRequest();
        val headers = HttpUtils.createBasicAuthHeaders("client", "secret");
        val authz = headers.get(AUTHORIZATION);
        assertNotNull(authz);
        request.addHeader(AUTHORIZATION, authz);
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        authenticator.validate(new CallContext(ctx, JEESessionStore.INSTANCE), credentials);
        assertNotNull(credentials.getUserProfile());
        assertEquals("casuser", credentials.getUserProfile().getId());
        assertTrue(((BasicUserProfile) credentials.getUserProfile()).getAuthenticationAttributes().size() >= 1);
    }
}
