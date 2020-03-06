package org.apereo.cas.support.oauth.authenticator;

import org.apereo.cas.services.RegisteredServiceAccessStrategyAuditableEnforcer;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuth20RegisteredServiceCipherExecutor;
import org.apereo.cas.util.EncodingUtils;

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

/**
 * This is {@link OAuth20RefreshTokenAuthenticator}.
 *
 * @author Julien Huon
 * @since 6.1.6
 */
@Tag("OAuth")
public class OAuth20RefreshTokenAuthenticatorTests extends BaseOAuth20AuthenticatorTests {
    protected OAuth20RefreshTokenAuthenticator authenticator;

    @BeforeEach
    public void init() {
        authenticator = new OAuth20RefreshTokenAuthenticator(servicesManager, serviceFactory,
            new RegisteredServiceAccessStrategyAuditableEnforcer(), ticketRegistry,
            new OAuth20RegisteredServiceCipherExecutor(),
            defaultPrincipalResolver);
    }

    @Test
    public void verifyAuthenticationWithClientWithoutSecret() {
        val refreshToken = getRefreshToken(serviceWithoutSecret);
        ticketRegistry.addTicket(refreshToken);

        val credentials = new UsernamePasswordCredentials("clientWithoutSecret", refreshToken.getId());
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.GRANT_TYPE, "refresh_token");
        request.addParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());
        request.addParameter(OAuth20Constants.REDIRECT_URI, "https://www.example2.org");

        val ctx = new JEEContext(request, new MockHttpServletResponse());
        authenticator.validate(credentials, ctx);
        assertNotNull(credentials.getUserProfile());
        assertEquals("clientWithoutSecret", credentials.getUserProfile().getId());


        val badClientIdCredentials = new UsernamePasswordCredentials("client", refreshToken.getId());
        val badClientIdRequest = new MockHttpServletRequest();
        badClientIdRequest.addParameter(OAuth20Constants.GRANT_TYPE, "refresh_token");
        badClientIdRequest.addParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());
        badClientIdRequest.addParameter(OAuth20Constants.REDIRECT_URI, "https://www.example.org");

        val badClientIdCtx = new JEEContext(badClientIdRequest, new MockHttpServletResponse());
        assertThrows(CredentialsException.class, () -> authenticator.validate(badClientIdCredentials, badClientIdCtx));


        val badRefreshTokenCredentials = new UsernamePasswordCredentials("clientWithoutSecret", "badRefreshToken");
        val badRefreshTokenRequest = new MockHttpServletRequest();
        badRefreshTokenRequest.addParameter(OAuth20Constants.GRANT_TYPE, "refresh_token");
        badRefreshTokenRequest.addParameter(OAuth20Constants.REFRESH_TOKEN, "badRefreshToken");
        badRefreshTokenRequest.addParameter(OAuth20Constants.REDIRECT_URI, "https://www.example.org");

        val badRefreshTokenCtx = new JEEContext(badRefreshTokenRequest, new MockHttpServletResponse());
        assertThrows(CredentialsException.class, () -> authenticator.validate(badRefreshTokenCredentials, badRefreshTokenCtx));
    }
}
