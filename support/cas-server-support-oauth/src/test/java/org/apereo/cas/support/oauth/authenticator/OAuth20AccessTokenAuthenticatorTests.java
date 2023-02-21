package org.apereo.cas.support.oauth.authenticator;

import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenEncoder;
import org.apereo.cas.ticket.InvalidTicketException;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.jee.context.session.JEESessionStore;
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

    @Override
    @BeforeEach
    public void initialize() {
        super.initialize();
        ticketRegistry.deleteAll();
    }

    @Test
    public void verifyAuthenticationWithJwtAccessToken() throws Exception {
        val accessToken = getAccessToken();
        this.ticketRegistry.addTicket(accessToken);

        val encoder = OAuth20JwtAccessTokenEncoder.builder()
            .accessToken(accessToken)
            .registeredService(serviceJwtAccessToken)
            .service(accessToken.getService())
            .accessTokenJwtBuilder(accessTokenJwtBuilder)
            .casProperties(casProperties)
            .build();

        val credentials = new TokenCredentials(encoder.encode(accessToken.getId()));
        val request = new MockHttpServletRequest();
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        authenticator.validate(new CallContext(ctx, JEESessionStore.INSTANCE), credentials);
        assertNotNull(credentials.getUserProfile());
    }

    @Test
    public void verifyAuthenticationFailsWithNoToken() {
        val accessToken = getAccessToken();
        val encoder = OAuth20JwtAccessTokenEncoder.builder()
            .accessToken(accessToken)
            .registeredService(serviceJwtAccessToken)
            .service(accessToken.getService())
            .accessTokenJwtBuilder(accessTokenJwtBuilder)
            .casProperties(casProperties)
            .build();
        val credentials = new TokenCredentials(encoder.encode(accessToken.getId()));
        val request = new MockHttpServletRequest();
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        assertThrows(InvalidTicketException.class,
            () -> authenticator.validate(new CallContext(ctx, JEESessionStore.INSTANCE), credentials));
    }

    @Test
    public void verifyAuthentication() throws Exception {
        val accessToken = getAccessToken();
        this.ticketRegistry.addTicket(accessToken);

        val encoder = OAuth20JwtAccessTokenEncoder.builder()
            .accessToken(accessToken)
            .registeredService(service)
            .service(accessToken.getService())
            .accessTokenJwtBuilder(accessTokenJwtBuilder)
            .casProperties(casProperties)
            .build();

        val credentials = new TokenCredentials(encoder.encode(accessToken.getId()));
        val request = new MockHttpServletRequest();
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        authenticator.validate(new CallContext(ctx, JEESessionStore.INSTANCE), credentials);
        assertNotNull(credentials.getUserProfile());
    }
}
