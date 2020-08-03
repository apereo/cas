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
import org.pac4j.core.exception.CredentialsException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20RefreshTokenAuthenticator}.
 *
 * @author Julien Huon
 * @since 6.2.0
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
    public void verifyAuthentication() {
        val refreshToken = getRefreshToken(serviceWithoutSecret);
        ticketRegistry.addTicket(refreshToken);


        val credentials = new UsernamePasswordCredentials("clientWithoutSecret", refreshToken.getId());
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name());
        request.addParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());
        request.addParameter(OAuth20Constants.CLIENT_ID, "clientWithoutSecret");

        val ctx = new JEEContext(request, new MockHttpServletResponse());
        authenticator.validate(credentials, ctx);
        assertNotNull(credentials.getUserProfile());
        assertEquals("clientWithoutSecret", credentials.getUserProfile().getId());


        val badRefreshTokenCredentials = new UsernamePasswordCredentials("clientWithoutSecret", "badRefreshToken");
        val badRefreshTokenRequest = new MockHttpServletRequest();
        badRefreshTokenRequest.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name());
        badRefreshTokenRequest.addParameter(OAuth20Constants.REFRESH_TOKEN, "badRefreshToken");
        badRefreshTokenRequest.addParameter(OAuth20Constants.CLIENT_ID, "clientWithoutSecret");

        val badRefreshTokenCtx = new JEEContext(badRefreshTokenRequest, new MockHttpServletResponse());
        assertThrows(CredentialsException.class, () -> authenticator.validate(badRefreshTokenCredentials, badRefreshTokenCtx));


        val badClientIdCredentials = new UsernamePasswordCredentials("clientWithoutSecret2", refreshToken.getId());
        val badClientIdRequest = new MockHttpServletRequest();
        badClientIdRequest.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name());
        badClientIdRequest.addParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());
        badClientIdRequest.addParameter(OAuth20Constants.CLIENT_ID, "clientWithoutSecret2");

        val badClientIdCtx = new JEEContext(badClientIdRequest, new MockHttpServletResponse());
        assertThrows(CredentialsException.class, () -> authenticator.validate(badClientIdCredentials, badClientIdCtx));


        val unsupportedClientRefreshToken = getRefreshToken(service);
        ticketRegistry.addTicket(unsupportedClientRefreshToken);


        val unsupportedClientCredentials = new UsernamePasswordCredentials("client", refreshToken.getId());
        val unsupportedClientRequest = new MockHttpServletRequest();
        unsupportedClientRequest.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name());
        unsupportedClientRequest.addParameter(OAuth20Constants.REFRESH_TOKEN, unsupportedClientRefreshToken.getId());
        unsupportedClientRequest.addParameter(OAuth20Constants.CLIENT_ID, "client");

        val unsupportedClientCtx = new JEEContext(unsupportedClientRequest, new MockHttpServletResponse());
        authenticator.validate(unsupportedClientCredentials, unsupportedClientCtx);
        assertNull(unsupportedClientCredentials.getUserProfile());


        val unknownClientCredentials = new UsernamePasswordCredentials("unknownclient", refreshToken.getId());
        val unknownclientRequest = new MockHttpServletRequest();
        unknownclientRequest.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name());
        unknownclientRequest.addParameter(OAuth20Constants.REFRESH_TOKEN, unsupportedClientRefreshToken.getId());
        unknownclientRequest.addParameter(OAuth20Constants.CLIENT_ID, "unknownclient");

        val unknownclientCtx = new JEEContext(unknownclientRequest, new MockHttpServletResponse());
        authenticator.validate(unknownClientCredentials, unknownclientCtx);
        assertNull(unknownClientCredentials.getUserProfile());
    }
}
