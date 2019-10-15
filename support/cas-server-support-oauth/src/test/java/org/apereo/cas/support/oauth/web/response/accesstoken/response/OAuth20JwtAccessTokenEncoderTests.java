package org.apereo.cas.support.oauth.web.response.accesstoken.response;

import org.apereo.cas.authentication.principal.AbstractWebApplicationService;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.AbstractOAuth20Tests;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.accesstoken.OAuth20JwtBuilder;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OAuth20JwtAccessTokenEncoderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OAuth")
public class OAuth20JwtAccessTokenEncoderTests extends AbstractOAuth20Tests {
    private static String encodeAccessToken(final AbstractWebApplicationService service,
                                            final AccessToken accessToken, final OAuth20JwtBuilder builder,
                                            final OAuthRegisteredService registeredService) {
        return OAuth20JwtAccessTokenEncoder.builder()
            .accessToken(accessToken)
            .registeredService(registeredService)
            .service(service)
            .accessTokenJwtBuilder(builder)
            .build()
            .encode();
    }

    @Test
    public void verifyAccessTokenIdEncodingWithoutJwt() {
        val tgt = new MockTicketGrantingTicket("casuser");
        val service = RegisteredServiceTestUtils.getService();
        val accessToken = mock(AccessToken.class);
        when(accessToken.getId()).thenReturn("ABCD");
        when(accessToken.getTicketGrantingTicket()).thenReturn(tgt);
        when(accessToken.getService()).thenReturn(service);

        val builder = new OAuth20JwtBuilder("http://cas.example.org/prefix",
            CipherExecutor.noOp(),
            mock(ServicesManager.class),
            RegisteredServiceCipherExecutor.noOp());

        val registeredService = getRegisteredService("example", "secret", new LinkedHashSet<>());
        val encodedAccessToken1 = encodeAccessToken(service, accessToken, builder, registeredService);
        assertNotNull(encodedAccessToken1);
        val encodedAccessToken2 = encodeAccessToken(service, accessToken, builder, registeredService);
        assertEquals(encodedAccessToken1, encodedAccessToken2);
    }

    @Test
    public void verifyAccessTokenIdEncodingWithJwt() {
        val tgt = new MockTicketGrantingTicket("casuser");
        val service = RegisteredServiceTestUtils.getService();

        val registeredService = getRegisteredService(service.getId(), "secret", new LinkedHashSet<>());
        registeredService.setJwtAccessToken(true);
        servicesManager.save(registeredService);

        val accessToken = mock(AccessToken.class);
        when(accessToken.getId()).thenReturn("ABCD");
        when(accessToken.getTicketGrantingTicket()).thenReturn(tgt);
        when(accessToken.getAuthentication()).thenReturn(tgt.getAuthentication());
        when(accessToken.getService()).thenReturn(service);
        when(accessToken.getExpirationPolicy()).thenReturn(NeverExpiresExpirationPolicy.INSTANCE);

        val builder = new OAuth20JwtBuilder("http://cas.example.org/prefix",
            CipherExecutor.noOp(),
            servicesManager,
            RegisteredServiceCipherExecutor.noOp());

        val encodedAccessToken1 = encodeAccessToken(service, accessToken, builder, registeredService);
        assertNotNull(encodedAccessToken1);
        val encodedAccessToken2 = encodeAccessToken(service, accessToken, builder, registeredService);
        assertEquals(encodedAccessToken1, encodedAccessToken2);
    }
}
