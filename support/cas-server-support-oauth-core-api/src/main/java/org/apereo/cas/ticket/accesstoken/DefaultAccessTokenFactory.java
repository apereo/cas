package org.apereo.cas.ticket.accesstoken;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.time.ZonedDateTime;
import java.util.Collection;

/**
 * Default OAuth access token factory.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
@RequiredArgsConstructor
@Getter
public class DefaultAccessTokenFactory implements AccessTokenFactory {

    /**
     * Default instance for the ticket id generator.
     */
    protected final UniqueTicketIdGenerator accessTokenIdGenerator;

    /**
     * ExpirationPolicy for refresh tokens.
     */
    protected final ExpirationPolicy expirationPolicy;

    /**
     * JWT builder instance.
     */
    protected final JwtBuilder jwtBuilder;

    public DefaultAccessTokenFactory(final ExpirationPolicy expirationPolicy, final JwtBuilder jwtBuilder) {
        this(new DefaultUniqueTicketIdGenerator(), expirationPolicy, jwtBuilder);
    }

    @Override
    public AccessToken create(final Service service, final Authentication authentication,
                              final TicketGrantingTicket ticketGrantingTicket,
                              final Collection<String> scopes, final String clientId) {
        var accessTokenId = this.accessTokenIdGenerator.getNewTicketId(AccessToken.PREFIX);

        val registeredService = (OAuthRegisteredService) this.jwtBuilder.getServicesManager().findServiceBy(service);
        if (registeredService != null && registeredService.isJwtAccessToken()) {
            val dt = ZonedDateTime.now().plusSeconds(this.expirationPolicy.getTimeToLive());
            val builder = JwtBuilder.JwtRequest.builder();
            val request = builder.serviceAudience(service.getId())
                .issueDate(DateTimeUtils.dateOf(authentication.getAuthenticationDate()))
                .jwtId(accessTokenId)
                .subject(authentication.getPrincipal().getId())
                .validUntilDate(DateTimeUtils.dateOf(dt))
                .attributes(authentication.getAttributes())
                .build();
            accessTokenId = jwtBuilder.build(request);
        }
        val at = new AccessTokenImpl(accessTokenId, service, authentication,
            this.expirationPolicy, ticketGrantingTicket, scopes, clientId);
        if (ticketGrantingTicket != null) {
            ticketGrantingTicket.getDescendantTickets().add(at.getId());
        }
        return at;
    }

    @Override
    public TicketFactory get(final Class<? extends Ticket> clazz) {
        return this;
    }
}
