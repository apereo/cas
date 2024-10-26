package org.apereo.cas.ticket.refreshtoken;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.RegisteredServiceDefinition;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import java.io.Serial;

/**
 * This is {@link OAuth20RefreshTokenExpirationPolicyBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public record OAuth20RefreshTokenExpirationPolicyBuilder(CasConfigurationProperties casProperties) implements ExpirationPolicyBuilder<OAuth20RefreshToken> {
    @Serial
    private static final long serialVersionUID = -3597980180617072826L;

    @Override
    public ExpirationPolicy buildTicketExpirationPolicy() {
        return toTicketExpirationPolicy();
    }

    /**
     * To ticket expiration policy.
     *
     * @return the expiration policy
     */
    public ExpirationPolicy toTicketExpirationPolicy() {
        val rtProps = casProperties.getAuthn().getOauth().getRefreshToken();
        val timeout = Beans.newDuration(rtProps.getTimeToKillInSeconds()).toSeconds();
        return buildExpirationPolicyFor(timeout);
    }

    @Override
    public ExpirationPolicy buildTicketExpirationPolicyFor(final RegisteredServiceDefinition registeredService) {
        if (registeredService instanceof final OAuthRegisteredService service && service.getRefreshTokenExpirationPolicy() != null) {
            val policy = service.getRefreshTokenExpirationPolicy();
            val timeToKill = policy.getTimeToKill();
            if (StringUtils.isNotBlank(timeToKill)) {
                val timeToKillInSeconds = Beans.newDuration(timeToKill).toSeconds();
                return buildExpirationPolicyFor(timeToKillInSeconds);
            }
        }
        return toTicketExpirationPolicy();
    }

    private OAuth20RefreshTokenExpirationPolicy buildExpirationPolicyFor(final long timeout) {
        if (casProperties.getTicket().isTrackDescendantTickets()) {
            return new OAuth20RefreshTokenExpirationPolicy(timeout);
        }
        return new OAuth20RefreshTokenStandaloneExpirationPolicy(timeout);
    }
}
