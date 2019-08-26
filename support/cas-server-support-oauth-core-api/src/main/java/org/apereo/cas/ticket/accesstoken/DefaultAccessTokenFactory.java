package org.apereo.cas.ticket.accesstoken;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
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
import org.apache.commons.lang3.StringUtils;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Map;

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
    protected final ExpirationPolicyBuilder<AccessToken> expirationPolicy;

    /**
     * JWT builder instance.
     */
    protected final JwtBuilder jwtBuilder;

    /**
     * Services manager.
     */
    protected final ServicesManager servicesManager;

    public DefaultAccessTokenFactory(final ExpirationPolicyBuilder<AccessToken> expirationPolicy,
                                     final JwtBuilder jwtBuilder,
                                     final ServicesManager servicesManager) {
        this(new DefaultUniqueTicketIdGenerator(), expirationPolicy, jwtBuilder, servicesManager);
    }

    @Override
    public AccessToken create(final Service service, final Authentication authentication,
                              final TicketGrantingTicket ticketGrantingTicket,
                              final Collection<String> scopes, final String clientId,
                              final Map<String, Map<String, Object>> requestClaims) {
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(jwtBuilder.getServicesManager(), clientId);
        val expirationPolicyToUse = determineExpirationPolicyForService(registeredService);
        val accessTokenId = this.accessTokenIdGenerator.getNewTicketId(AccessToken.PREFIX);
        val at = new AccessTokenImpl(accessTokenId, service, authentication,
                expirationPolicyToUse, ticketGrantingTicket, scopes,
                clientId, requestClaims);
        if (registeredService != null && registeredService.isJwtAccessToken()) {
            val dt = ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(expirationPolicyToUse.getTimeToLive());
            val builder = JwtBuilder.JwtRequest.builder();

            val request = builder
                .serviceAudience(service.getId())
                .issueDate(DateTimeUtils.dateOf(authentication.getAuthenticationDate()))
                .jwtId(accessTokenId)
                .subject(authentication.getPrincipal().getId())
                .validUntilDate(DateTimeUtils.dateOf(dt))
                .attributes(authentication.getAttributes())
                .build();
            at.setJwt(jwtBuilder.build(request));
        }

        if (ticketGrantingTicket != null) {
            ticketGrantingTicket.getDescendantTickets().add(at.getId());
        }
        return at;
    }

    @Override
    public AccessToken create(final Service service, final Authentication authentication,
                              final Collection<String> scopes, final String clientId,
                              final Map<String, Map<String, Object>> requestClaims) {
        val accessTokenId = this.accessTokenIdGenerator.getNewTicketId(AccessToken.PREFIX);
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(jwtBuilder.getServicesManager(), clientId);
        val expirationPolicyToUse = determineExpirationPolicyForService(registeredService);
        return new AccessTokenImpl(accessTokenId, service, authentication,
            expirationPolicyToUse, null,
            scopes, clientId, requestClaims);
    }

    @Override
    public TicketFactory get(final Class<? extends Ticket> clazz) {
        return this;
    }

    private ExpirationPolicy determineExpirationPolicyForService(final OAuthRegisteredService registeredService) {
        if (registeredService != null && registeredService.getAccessTokenExpirationPolicy() != null) {
            val policy = registeredService.getAccessTokenExpirationPolicy();
            val maxTime = policy.getMaxTimeToLive();
            val ttl = policy.getTimeToKill();
            if (StringUtils.isNotBlank(maxTime) && StringUtils.isNotBlank(ttl)) {
                return new OAuthAccessTokenExpirationPolicy(
                    Beans.newDuration(maxTime).getSeconds(),
                    Beans.newDuration(ttl).getSeconds());
            }
        }
        return this.expirationPolicy.buildTicketExpirationPolicy();
    }
}
