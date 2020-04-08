package org.apereo.cas.support.oauth.authenticator;

import org.apereo.cas.services.RegisteredServiceAccessStrategyAuditableEnforcer;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.services.OAuth20RegisteredServiceCipherExecutor;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20ClientIdClientSecretAuthenticatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("OAuth")
public class OAuth20ClientIdClientSecretAuthenticatorTests extends BaseOAuth20AuthenticatorTests {
    protected OAuth20ClientIdClientSecretAuthenticator authenticator;

    @BeforeEach
    public void init() {
        authenticator = new OAuth20ClientIdClientSecretAuthenticator(servicesManager, serviceFactory,
            new RegisteredServiceAccessStrategyAuditableEnforcer(),
            new OAuth20RegisteredServiceCipherExecutor(),
            ticketRegistry,
            defaultPrincipalResolver);
    }

    @Test
    public void verifyAuthentication() {
        val credentials = new UsernamePasswordCredentials("client", "secret");
        val request = new MockHttpServletRequest();
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        authenticator.validate(credentials, ctx);
        assertNotNull(credentials.getUserProfile());
        assertEquals("client", credentials.getUserProfile().getId());
    }

    @Test
    public void verifyAuthenticationWithGrantTypePassword() {
        val credentials = new UsernamePasswordCredentials("client", "secret");
        val request = new MockHttpServletRequest();
        val ctx = new JEEContext(request, new MockHttpServletResponse());

        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PASSWORD.name());

        authenticator.validate(credentials, ctx);
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

        assertFalse(authenticator.canAuthenticate(ctx));
        authenticator.validate(credentials, ctx);
        assertNull(credentials.getUserProfile());


        request.removeAllParameters();
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name());
        request.addParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());
        assertTrue(authenticator.canAuthenticate(ctx));


        request.removeAllParameters();
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name());
        request.addParameter(OAuth20Constants.CLIENT_ID, "serviceWithoutSecret");
        request.addParameter(OAuth20Constants.CLIENT_SECRET, "serviceWithoutSecret");
        request.addParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());
        assertTrue(authenticator.canAuthenticate(ctx));
    }
}
