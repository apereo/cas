package org.apereo.cas.ticket.accesstoken;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.val;

import java.io.Serial;

/**
 * This is {@link OAuth20AccessTokenExpirationPolicyBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public record OAuth20AccessTokenExpirationPolicyBuilder(CasConfigurationProperties casProperties) implements ExpirationPolicyBuilder<OAuth20AccessToken> {
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
        val oauth = casProperties.getAuthn().getOauth().getAccessToken();
        if (casProperties.getTicket().isTrackDescendantTickets()) {
            return new OAuth20AccessTokenExpirationPolicy(
                Beans.newDuration(oauth.getMaxTimeToLiveInSeconds()).toSeconds(),
                Beans.newDuration(oauth.getTimeToKillInSeconds()).toSeconds()
            );
        }
        return new OAuth20AccessTokenExpirationPolicy.OAuthAccessTokenSovereignExpirationPolicy(
            Beans.newDuration(oauth.getMaxTimeToLiveInSeconds()).toSeconds(),
            Beans.newDuration(oauth.getTimeToKillInSeconds()).toSeconds()
        );
    }
}
