package org.apereo.cas.ticket.refreshtoken;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.services.RegisteredServiceOAuthRefreshTokenExpirationPolicy;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.tracking.TicketTrackingPolicy;
import org.apereo.cas.util.HostNameBasedUniqueTicketIdGenerator;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Default OAuth refresh token factory.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
@RequiredArgsConstructor
public class OAuth20DefaultRefreshTokenFactory implements OAuth20RefreshTokenFactory {

    @Getter
    protected final UniqueTicketIdGenerator ticketIdGenerator;

    protected final TicketRegistry ticketRegistry;

    @Getter
    protected final ExpirationPolicyBuilder<OAuth20RefreshToken> expirationPolicyBuilder;

    protected final ServicesManager servicesManager;

    protected final TicketTrackingPolicy descendantTicketsTrackingPolicy;

    protected final CasConfigurationProperties casProperties;

    public OAuth20DefaultRefreshTokenFactory(final ExpirationPolicyBuilder<OAuth20RefreshToken> expirationPolicyBuilder,
                                             final TicketRegistry ticketRegistry,
                                             final ServicesManager servicesManager,
                                             final TicketTrackingPolicy descendantTicketsTrackingPolicy,
                                             final CasConfigurationProperties casProperties) {
        this(new HostNameBasedUniqueTicketIdGenerator(), ticketRegistry, expirationPolicyBuilder,
            servicesManager, descendantTicketsTrackingPolicy, casProperties);
    }

    @Override
    public OAuth20RefreshToken create(final Service service,
                                      final Authentication authentication,
                                      final Ticket ticketGrantingTicket,
                                      final Collection<String> scopes,
                                      final String clientId,
                                      final String accessToken,
                                      final Map<String, Map<String, Object>> requestClaims,
                                      final OAuth20ResponseTypes responseType,
                                      final OAuth20GrantTypes grantType) throws Throwable {
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, clientId);

        var limitReached = false;
        if (ticketGrantingTicket != null) {
            val maxNumberOfTokensAllowed = getMaxNumberOfRefreshTokensAllowed(registeredService);
            if (maxNumberOfTokensAllowed > 0) {
                limitReached = descendantTicketsTrackingPolicy.countTicketsFor(ticketGrantingTicket, service) >= maxNumberOfTokensAllowed
                    || ticketRegistry.countTicketsFor(service) >= maxNumberOfTokensAllowed;
            }
            FunctionUtils.throwIf(limitReached, () -> new IllegalArgumentException("Refresh token limit for %s is reached".formatted(service.getId())));
        }
        val codeId = ticketIdGenerator.getNewTicketId(OAuth20RefreshToken.PREFIX);
        val expirationPolicyToUse = determineExpirationPolicyForService(registeredService);
        val refreshToken = new OAuth20DefaultRefreshToken(codeId, service, authentication,
            expirationPolicyToUse, ticketGrantingTicket,
            scopes, clientId, accessToken, requestClaims, responseType, grantType);
        FunctionUtils.doIfNotNull(service, __ -> refreshToken.setTenantId(service.getTenant()));
        descendantTicketsTrackingPolicy.trackTicket(ticketGrantingTicket, refreshToken);
        return refreshToken;
    }

    private long getMaxNumberOfRefreshTokensAllowed(final OAuthRegisteredService registeredService) {
        return Optional.ofNullable(registeredService)
            .map(OAuthRegisteredService::getRefreshTokenExpirationPolicy)
            .map(RegisteredServiceOAuthRefreshTokenExpirationPolicy::getMaxActiveTokens)
            .orElseGet(() -> casProperties.getAuthn().getOauth().getRefreshToken().getMaxActiveTokensAllowed());
    }

    protected ExpirationPolicy determineExpirationPolicyForService(final RegisteredService registeredService) {
        return expirationPolicyBuilder.buildTicketExpirationPolicyFor(registeredService);
    }

    @Override
    public Class<? extends Ticket> getTicketType() {
        return OAuth20RefreshToken.class;
    }
}
