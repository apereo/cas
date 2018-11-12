package org.apereo.cas.support.oauth.authenticator;

import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.util.HttpUtils;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.exception.CredentialsException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20UsernamePasswordAuthenticatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class OAuth20UsernamePasswordAuthenticatorTests extends BaseOAuth20AuthenticatorTests {
    protected OAuth20UsernamePasswordAuthenticator authenticator;

    @Override
    public void initialize() {
        super.initialize();
        authenticator = new OAuth20UsernamePasswordAuthenticator(authenticationSystemSupport, servicesManager, serviceFactory);
    }

    @Test
    public void verifyAcceptedCredentialsWithClientId() {
        val credentials = new UsernamePasswordCredentials("casuser", "casuser");
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, "client");
        val ctx = new J2EContext(request, new MockHttpServletResponse());
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
        val ctx = new J2EContext(request, new MockHttpServletResponse());
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
        val ctx = new J2EContext(request, new MockHttpServletResponse());
        assertThrows(CredentialsException.class, () -> authenticator.validate(credentials, ctx));
    }

    @Test
    public void verifyAcceptedCredentialsWithServiceDisabled() {
        val credentials = new UsernamePasswordCredentials("casuser", "casuser");
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, "client");
        service.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(false, false));
        val ctx = new J2EContext(request, new MockHttpServletResponse());
        assertThrows(CredentialsException.class, () -> authenticator.validate(credentials, ctx));
    }

    @Test
    public void verifyAcceptedCredentialsWithBadCredentials() {
        val credentials = new UsernamePasswordCredentials("casuser-something", "casuser");
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, "client");
        val ctx = new J2EContext(request, new MockHttpServletResponse());
        assertThrows(CredentialsException.class, () -> authenticator.validate(credentials, ctx));
    }

    @Test
    public void verifyAcceptedCredentialsWithoutClientId() {
        val credentials = new UsernamePasswordCredentials("casuser", "casuser");
        val request = new MockHttpServletRequest();
        val ctx = new J2EContext(request, new MockHttpServletResponse());
        assertThrows(CredentialsException.class, () -> authenticator.validate(credentials, ctx));
    }

    @Test
    public void verifyAcceptedCredentialsWithClientSecretWithBasicAuth() {
        val credentials = new UsernamePasswordCredentials("casuser", "casuser");
        val request = new MockHttpServletRequest();
        val headers = HttpUtils.createBasicAuthHeaders("client", "secret");
        request.addHeader(org.springframework.http.HttpHeaders.AUTHORIZATION, headers.get(org.springframework.http.HttpHeaders.AUTHORIZATION));
        val ctx = new J2EContext(request, new MockHttpServletResponse());
        authenticator.validate(credentials, ctx);
        assertNotNull(credentials.getUserProfile());
        assertEquals("casuser", credentials.getUserProfile().getId());
    }
}
