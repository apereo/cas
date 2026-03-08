package org.apereo.cas.ticket.accesstoken;

import module java.base;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.RegisteredServiceDefinition;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.val;

/**
 * This is {@link OAuth20AccessTokenExpirationPolicyBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public record OAuth20AccessTokenExpirationPolicyBuilder(CasConfigurationProperties casProperties)
        implements ExpirationPolicyBuilder<OAuth20AccessToken> {
    @Serial
    private static final long serialVersionUID = -3597980180617072826L;

    @Override
    public ExpirationPolicy buildTicketExpirationPolicy() {
        return toTicketExpirationPolicy();
    }

    @Override
    public ExpirationPolicy buildTicketExpirationPolicyFor(final RegisteredServiceDefinition registeredService) {
        if (registeredService instanceof final OAuthRegisteredService service && service.getAccessTokenExpirationPolicy() != null) {
            val policy = service.getAccessTokenExpirationPolicy();
            val maxTime = policy.getMaxTimeToLive();
            val ttl = policy.getTimeToKill();
            if (StringUtils.isNotBlank(maxTime) && StringUtils.isNotBlank(ttl)) {
                val maxTimeInSeconds = Beans.newDuration(maxTime).toSeconds();
                val ttlInSeconds = Beans.newDuration(ttl).toSeconds();
                return buildExpirationPolicyFor(maxTimeInSeconds, ttlInSeconds);
            }
        }
        return toTicketExpirationPolicy();
    }

    /**
     * To ticket expiration policy.
     *
     * @return the expiration policy
     */
    public ExpirationPolicy toTicketExpirationPolicy() {
        val oauth = casProperties.getAuthn().getOauth().getAccessToken();
        val maxTimeInSeconds = Beans.newDuration(oauth.getMaxTimeToLiveInSeconds()).toSeconds();
        val ttlInSeconds = Beans.newDuration(oauth.getTimeToKillInSeconds()).toSeconds();
        return buildExpirationPolicyFor(maxTimeInSeconds, ttlInSeconds);
    }

    private OAuth20AccessTokenExpirationPolicy buildExpirationPolicyFor(final long maxTimeToLive, final long timeToKill) {
        if (casProperties.getTicket().isTrackDescendantTickets()) {
            return new OAuth20AccessTokenExpirationPolicy(maxTimeToLive, timeToKill);
        }
        return new OAuth20AccessTokenExpirationPolicy.OAuthAccessTokenSovereignExpirationPolicy(maxTimeToLive, timeToKill);
    }
}
