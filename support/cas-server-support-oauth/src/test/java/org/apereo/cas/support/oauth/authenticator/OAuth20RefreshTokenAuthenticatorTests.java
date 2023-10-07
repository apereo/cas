package org.apereo.cas.support.oauth.authenticator;

import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junitpioneer.jupiter.RetryingTest;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.jee.context.session.JEESessionStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
class OAuth20RefreshTokenAuthenticatorTests extends BaseOAuth20AuthenticatorTests {
    @Autowired
    @Qualifier("oauthRefreshTokenAuthenticator")
    private Authenticator authenticator;

    @RetryingTest(3)
    void verifyAuthentication() throws Throwable {
        val refreshToken = getRefreshToken(serviceWithoutSecret);
        ticketRegistry.addTicket(refreshToken);

        val credentials = new UsernamePasswordCredentials("clientWithoutSecret", refreshToken.getId());
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name());
        request.addParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());
        request.addParameter(OAuth20Constants.CLIENT_ID, "clientWithoutSecret");

        val ctx = new JEEContext(request, new MockHttpServletResponse());
        authenticator.validate(new CallContext(ctx, new JEESessionStore()), credentials);
        assertNotNull(credentials.getUserProfile());
        assertEquals("clientWithoutSecret", credentials.getUserProfile().getId());


        val badRefreshTokenCredentials = new UsernamePasswordCredentials("clientWithoutSecret", "badRefreshToken");
        val badRefreshTokenRequest = new MockHttpServletRequest();
        badRefreshTokenRequest.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name());
        badRefreshTokenRequest.addParameter(OAuth20Constants.REFRESH_TOKEN, "badRefreshToken");
        badRefreshTokenRequest.addParameter(OAuth20Constants.CLIENT_ID, "clientWithoutSecret");

        val badRefreshTokenCtx = new JEEContext(badRefreshTokenRequest, new MockHttpServletResponse());
        assertThrows(CredentialsException.class,
            () -> authenticator.validate(new CallContext(badRefreshTokenCtx, new JEESessionStore()), badRefreshTokenCredentials));


        val badClientIdCredentials = new UsernamePasswordCredentials(serviceWithoutSecret2.getClientId(), refreshToken.getId());
        val badClientIdRequest = new MockHttpServletRequest();
        badClientIdRequest.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name());
        badClientIdRequest.addParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());
        badClientIdRequest.addParameter(OAuth20Constants.CLIENT_ID, serviceWithoutSecret2.getClientId());

        val badClientIdCtx = new JEEContext(badClientIdRequest, new MockHttpServletResponse());
        assertThrows(CredentialsException.class,
            () -> authenticator.validate(new CallContext(badClientIdCtx, new JEESessionStore()), badClientIdCredentials));

        val unsupportedClientRefreshToken = getRefreshToken(service);
        ticketRegistry.addTicket(unsupportedClientRefreshToken);

        val unsupportedClientCredentials = new UsernamePasswordCredentials("client", refreshToken.getId());
        val unsupportedClientRequest = new MockHttpServletRequest();
        unsupportedClientRequest.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name());
        unsupportedClientRequest.addParameter(OAuth20Constants.REFRESH_TOKEN, unsupportedClientRefreshToken.getId());
        unsupportedClientRequest.addParameter(OAuth20Constants.CLIENT_ID, "client");

        val unsupportedClientCtx = new JEEContext(unsupportedClientRequest, new MockHttpServletResponse());
        authenticator.validate(new CallContext(unsupportedClientCtx, new JEESessionStore()), unsupportedClientCredentials);
        assertNull(unsupportedClientCredentials.getUserProfile());

        val unknownClientCredentials = new UsernamePasswordCredentials("unknownclient", refreshToken.getId());
        val unknownclientRequest = new MockHttpServletRequest();
        unknownclientRequest.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name());
        unknownclientRequest.addParameter(OAuth20Constants.REFRESH_TOKEN, unsupportedClientRefreshToken.getId());
        unknownclientRequest.addParameter(OAuth20Constants.CLIENT_ID, "unknownclient");

        val unknownclientCtx = new JEEContext(unknownclientRequest, new MockHttpServletResponse());
        authenticator.validate(new CallContext(unknownclientCtx, new JEESessionStore()), unknownClientCredentials);
        assertNull(unknownClientCredentials.getUserProfile());
    }
}
