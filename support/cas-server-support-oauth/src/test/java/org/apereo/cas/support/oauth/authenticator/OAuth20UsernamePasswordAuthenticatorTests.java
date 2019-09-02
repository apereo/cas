package org.apereo.cas.support.oauth.authenticator;

import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuth20RegisteredServiceCipherExecutor;
import org.apereo.cas.util.HttpUtils;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.exception.CredentialsException;
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
public class OAuth20UsernamePasswordAuthenticatorTests extends BaseOAuth20AuthenticatorTests {
    protected OAuth20UsernamePasswordAuthenticator authenticator;

    @BeforeEach
    public void init() {
        authenticator = new OAuth20UsernamePasswordAuthenticator(authenticationSystemSupport,
            servicesManager, serviceFactory,
            new OAuth20RegisteredServiceCipherExecutor());
    }

    @Test
    public void verifyAcceptedCredentialsWithClientId() {
        val credentials = new UsernamePasswordCredentials("casuser", "casuser");
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, "clientWithoutSecret");
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        authenticator.validate(credentials, ctx);
        assertNotNull(credentials.getUserProfile());
        assertEquals("casuser", credentials.getUserProfile().getId());
    }

    @Test
    public void verifyAcceptedCredentialsWithClientSecret() {
        val credentials = new UsernamePasswordCredentials("casuser", "casuser");
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, "client");
        request.addParameter(OAuth20Constants.CLIENT_SECRET, "secret");
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        authenticator.validate(credentials, ctx);
        assertNotNull(credentials.getUserProfile());
        assertEquals("casuser", credentials.getUserProfile().getId());
    }

    @Test
    public void verifyAcceptedCredentialsWithBadClientSecret() {
        val credentials = new UsernamePasswordCredentials("casuser", "casuser");
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, "client");
        request.addParameter(OAuth20Constants.CLIENT_SECRET, "secretnotfound");
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        assertThrows(CredentialsException.class, () -> authenticator.validate(credentials, ctx));
    }

    @Test
    public void verifyAcceptedCredentialsWithServiceDisabled() {
        val credentials = new UsernamePasswordCredentials("casuser", "casuser");
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, "client");
        service.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(false, false));
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        assertThrows(CredentialsException.class, () -> authenticator.validate(credentials, ctx));
    }

    @Test
    public void verifyAcceptedCredentialsWithBadCredentials() {
        val credentials = new UsernamePasswordCredentials("casuser-something", "casuser");
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, "client");
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        assertThrows(CredentialsException.class, () -> authenticator.validate(credentials, ctx));
    }

    @Test
    public void verifyAcceptedCredentialsWithoutClientSecret() {
        val credentials = new UsernamePasswordCredentials("casuser", "casuser");
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, "client");
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        assertThrows(CredentialsException.class, () -> authenticator.validate(credentials, ctx));
    }

    @Test
    public void verifyAcceptedCredentialsWithoutClientId() {
        val credentials = new UsernamePasswordCredentials("casuser", "casuser");
        val request = new MockHttpServletRequest();
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        assertThrows(CredentialsException.class, () -> authenticator.validate(credentials, ctx));
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
        authenticator.validate(credentials, ctx);
        assertNotNull(credentials.getUserProfile());
        assertEquals("casuser", credentials.getUserProfile().getId());
    }
}
