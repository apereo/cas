package org.apereo.cas.ticket.accesstoken;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.services.RegisteredServiceOAuthAccessTokenExpirationPolicy;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.tracking.TicketTrackingPolicy;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.HostNameBasedUniqueTicketIdGenerator;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Default OAuth access token factory.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
@RequiredArgsConstructor
@Getter
public class OAuth20DefaultAccessTokenFactory implements OAuth20AccessTokenFactory {

    protected final UniqueTicketIdGenerator accessTokenIdGenerator;

    protected final TicketRegistry ticketRegistry;

    protected final ExpirationPolicyBuilder<OAuth20AccessToken> expirationPolicyBuilder;

    protected final JwtBuilder jwtBuilder;

    protected final ServicesManager servicesManager;

    protected final TicketTrackingPolicy descendantTicketsTrackingPolicy;

    public OAuth20DefaultAccessTokenFactory(
        final TicketRegistry ticketRegistry,
        final ExpirationPolicyBuilder<OAuth20AccessToken> expirationPolicyBuilder,
        final JwtBuilder jwtBuilder,
        final ServicesManager servicesManager,
        final TicketTrackingPolicy descendantTicketsTrackingPolicy) {
        this(new HostNameBasedUniqueTicketIdGenerator(), ticketRegistry, expirationPolicyBuilder,
            jwtBuilder, servicesManager, descendantTicketsTrackingPolicy);
    }

    @Override
    public OAuth20AccessToken create(final Service service,
                                     final Authentication authentication,
                                     final Ticket ticketGrantingTicket,
                                     final Collection<String> scopes,
                                     final String exchangedToken,
                                     final String clientId,
                                     final Map<String, Map<String, Object>> requestClaims,
                                     final OAuth20ResponseTypes responseType,
                                     final OAuth20GrantTypes grantType) throws Throwable {
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(jwtBuilder.getServicesManager(), clientId);
        var limitReached = false;
        if (ticketGrantingTicket != null) {
            val maxNumberOfTokensAllowed = getMaxNumberOfAccessTokensAllowed(registeredService);
            if (maxNumberOfTokensAllowed > 0) {
                limitReached = descendantTicketsTrackingPolicy.countTicketsFor(ticketGrantingTicket, service) >= maxNumberOfTokensAllowed
                    || ticketRegistry.countTicketsFor(service) >= maxNumberOfTokensAllowed;
            }
            FunctionUtils.throwIf(limitReached, () -> new IllegalArgumentException("Access token limit for %s is reached".formatted(service.getId())));
        }
        val expirationPolicyToUse = determineExpirationPolicyForService(registeredService);
        val accessTokenId = generateAccessTokenId(service, authentication);
        val accessToken = new OAuth20DefaultAccessToken(accessTokenId, service, authentication,
            expirationPolicyToUse, ticketGrantingTicket, exchangedToken, scopes,
            clientId, requestClaims, responseType, grantType);
        FunctionUtils.doIfNotNull(service, __ -> accessToken.setTenantId(service.getTenant()));
        descendantTicketsTrackingPolicy.trackTicket(ticketGrantingTicket, accessToken);
        return accessToken;
    }

    private long getMaxNumberOfAccessTokensAllowed(final OAuthRegisteredService registeredService) {
        return Optional.ofNullable(registeredService)
            .map(OAuthRegisteredService::getAccessTokenExpirationPolicy)
            .map(RegisteredServiceOAuthAccessTokenExpirationPolicy::getMaxActiveTokens)
            .orElseGet(() -> jwtBuilder.getCasProperties().getAuthn().getOauth().getAccessToken().getMaxActiveTokensAllowed());
    }

    protected String generateAccessTokenId(final Service service, final Authentication authentication) throws Throwable {
        return accessTokenIdGenerator.getNewTicketId(OAuth20AccessToken.PREFIX);
    }

    @Override
    public Class<? extends Ticket> getTicketType() {
        return OAuth20AccessToken.class;
    }

    protected ExpirationPolicy determineExpirationPolicyForService(final OAuthRegisteredService registeredService) {
        if (registeredService != null && registeredService.getAccessTokenExpirationPolicy() != null) {
            val policy = registeredService.getAccessTokenExpirationPolicy();
            val maxTime = policy.getMaxTimeToLive();
            val ttl = policy.getTimeToKill();
            if (StringUtils.isNotBlank(maxTime) && StringUtils.isNotBlank(ttl)) {
                return new OAuth20AccessTokenExpirationPolicy(
                    Beans.newDuration(maxTime).toSeconds(),
                    Beans.newDuration(ttl).toSeconds());
            }
        }
        return expirationPolicyBuilder.buildTicketExpirationPolicy();
    }
}
