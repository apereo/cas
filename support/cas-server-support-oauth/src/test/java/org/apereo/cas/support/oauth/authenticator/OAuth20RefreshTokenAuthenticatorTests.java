package org.apereo.cas.support.oauth.authenticator;

import module java.base;
import org.apereo.cas.OAuth20TestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junitpioneer.jupiter.RetryingTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
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
    private OAuth20ClientCredentialsAuthenticator authenticator;

    @RetryingTest(3)
    void verifyAuthentication() throws Throwable {
        val refreshToken = getRefreshToken(serviceWithoutSecret);
        ticketRegistry.addTicket(refreshToken);

        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name());
        request.addParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());
        request.addParameter(OAuth20Constants.CLIENT_ID, "clientWithoutSecret");

        val credentialsProfile = authenticator.authenticate(OAuth20TestUtils.newClientAuthenticationToken(request, "clientWithoutSecret", refreshToken.getId()));
        assertTrue(credentialsProfile.isPresent());
        assertEquals("clientWithoutSecret", credentialsProfile.orElseThrow().getId());


        val badRefreshTokenRequest = new MockHttpServletRequest();
        badRefreshTokenRequest.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name());
        badRefreshTokenRequest.addParameter(OAuth20Constants.REFRESH_TOKEN, "badRefreshToken");
        badRefreshTokenRequest.addParameter(OAuth20Constants.CLIENT_ID, "clientWithoutSecret");

        assertThrows(BadCredentialsException.class,
            () -> authenticator.authenticate(OAuth20TestUtils.newClientAuthenticationToken(badRefreshTokenRequest, "clientWithoutSecret", "badRefreshToken")));


        val badClientIdRequest = new MockHttpServletRequest();
        badClientIdRequest.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name());
        badClientIdRequest.addParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());
        badClientIdRequest.addParameter(OAuth20Constants.CLIENT_ID, serviceWithoutSecret2.getClientId());

        assertThrows(BadCredentialsException.class,
            () -> authenticator.authenticate(OAuth20TestUtils.newClientAuthenticationToken(badClientIdRequest, serviceWithoutSecret2.getClientId(), refreshToken.getId())));

        val unsupportedClientRefreshToken = getRefreshToken(service);
        ticketRegistry.addTicket(unsupportedClientRefreshToken);

        val unsupportedClientRequest = new MockHttpServletRequest();
        unsupportedClientRequest.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name());
        unsupportedClientRequest.addParameter(OAuth20Constants.REFRESH_TOKEN, unsupportedClientRefreshToken.getId());
        unsupportedClientRequest.addParameter(OAuth20Constants.CLIENT_ID, "client");

        val unsupportedClientCredentialsProfile = authenticator.authenticate(OAuth20TestUtils.newClientAuthenticationToken(unsupportedClientRequest, "client", refreshToken.getId()));
        assertTrue(unsupportedClientCredentialsProfile.isEmpty());

        val unknownclientRequest = new MockHttpServletRequest();
        unknownclientRequest.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name());
        unknownclientRequest.addParameter(OAuth20Constants.REFRESH_TOKEN, unsupportedClientRefreshToken.getId());
        unknownclientRequest.addParameter(OAuth20Constants.CLIENT_ID, "unknownclient");

        val unknownClientCredentialsProfile = authenticator.authenticate(OAuth20TestUtils.newClientAuthenticationToken(unknownclientRequest, "unknownclient", refreshToken.getId()));
        assertTrue(unknownClientCredentialsProfile.isEmpty());
    }
}
