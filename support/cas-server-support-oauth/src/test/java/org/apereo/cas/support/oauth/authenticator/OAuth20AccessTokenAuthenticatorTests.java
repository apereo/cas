package org.apereo.cas.support.oauth.authenticator;

import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenEncoder;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.credentials.TokenCredentials;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20AccessTokenAuthenticatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OAuth")
public class OAuth20AccessTokenAuthenticatorTests extends BaseOAuth20AuthenticatorTests {
    protected OAuth20AccessTokenAuthenticator authenticator;

    @BeforeEach
    public void init() {
        authenticator = new OAuth20AccessTokenAuthenticator(ticketRegistry, accessTokenJwtBuilder);
    }

    @Test
    public void verifyAuthenticationWithJwtAccessToken() {
        val accessToken = getAccessToken();
        this.ticketRegistry.addTicket(accessToken);

        val encoder = OAuth20JwtAccessTokenEncoder.builder()
            .accessToken(accessToken)
            .registeredService(serviceJwtAccessToken)
            .service(accessToken.getService())
            .accessTokenJwtBuilder(accessTokenJwtBuilder)
            .casProperties(casProperties)
            .build();

        val credentials = new TokenCredentials(encoder.encode());
        val request = new MockHttpServletRequest();
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        authenticator.validate(credentials, ctx);
        assertNotNull(credentials.getUserProfile());
    }

    @Test
    public void verifyAuthentication() {
        val accessToken = getAccessToken();
        this.ticketRegistry.addTicket(accessToken);

        val encoder = OAuth20JwtAccessTokenEncoder.builder()
            .accessToken(accessToken)
            .registeredService(service)
            .service(accessToken.getService())
            .accessTokenJwtBuilder(accessTokenJwtBuilder)
            .casProperties(casProperties)
            .build();

        val credentials = new TokenCredentials(encoder.encode());
        val request = new MockHttpServletRequest();
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        authenticator.validate(credentials, ctx);
        assertNotNull(credentials.getUserProfile());
    }
}
