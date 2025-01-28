package org.apereo.cas;

import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import lombok.experimental.UtilityClass;
import lombok.val;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import static org.mockito.Mockito.*;

/**
 * This is {@link OAuth20TestUtils}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@UtilityClass
public class OAuth20TestUtils {
    /**
     * Gets access token.
     *
     * @param ticketGrantingTicket the ticket granting ticket
     * @param id                   the id
     * @param serviceId            the service id
     * @param clientId             the client id
     * @return the access token
     */
    public static OAuth20AccessToken getAccessToken(final TicketGrantingTicket ticketGrantingTicket,
                                                       final String id,
                                                       final String serviceId, final String clientId) {
        val service = RegisteredServiceTestUtils.getService(serviceId);
        service.getAttributes().put(OAuth20Constants.CLIENT_ID, List.of(clientId));
        val accessToken = mock(OAuth20AccessToken.class);
        val ticketId = OAuth20AccessToken.PREFIX + "-%s".formatted(id);
        when(accessToken.getId()).thenReturn(ticketId);
        when(accessToken.getTicketGrantingTicket()).thenReturn(ticketGrantingTicket);
        when(accessToken.getAuthentication()).thenReturn(ticketGrantingTicket.getAuthentication());
        when(accessToken.getService()).thenReturn(service);
        when(accessToken.getClientId()).thenReturn(clientId);
        when(accessToken.getExpirationPolicy()).thenReturn(NeverExpiresExpirationPolicy.INSTANCE);
        when(accessToken.getCreationTime()).thenReturn(ZonedDateTime.now(Clock.systemUTC()));
        when(accessToken.toString()).thenReturn(ticketId);
        when(accessToken.getGrantType()).thenReturn(OAuth20GrantTypes.AUTHORIZATION_CODE);
        return accessToken;
    }

    /**
     * Gets refresh token.
     *
     * @param serviceId the service id
     * @param clientId  the client id
     * @return the refresh token
     */
    public static OAuth20RefreshToken getRefreshToken(final String serviceId, final String clientId) {
        val tgt = new MockTicketGrantingTicket(clientId);
        val refreshToken = mock(OAuth20RefreshToken.class);
        val service = RegisteredServiceTestUtils.getService(serviceId);
        when(refreshToken.getService()).thenReturn(service);
        service.getAttributes().put(OAuth20Constants.CLIENT_ID, List.of(clientId));
        when(refreshToken.getCreationTime()).thenReturn(ZonedDateTime.now(Clock.systemUTC()));
        val ticketId = OAuth20RefreshToken.PREFIX + "-%s".formatted(UUID.randomUUID().toString());
        when(refreshToken.getId()).thenReturn(ticketId);
        when(refreshToken.getTicketGrantingTicket()).thenReturn(tgt);
        when(refreshToken.getAuthentication()).thenReturn(tgt.getAuthentication());
        when(refreshToken.getClientId()).thenReturn(clientId);
        when(refreshToken.getExpirationPolicy()).thenReturn(NeverExpiresExpirationPolicy.INSTANCE);
        when(refreshToken.toString()).thenReturn(ticketId);
        return refreshToken;
    }
}
