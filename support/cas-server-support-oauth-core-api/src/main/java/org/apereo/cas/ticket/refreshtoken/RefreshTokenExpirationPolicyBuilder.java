package org.apereo.cas.ticket.refreshtoken;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * This is {@link RefreshTokenExpirationPolicyBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiredArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@ToString
@Slf4j
@Getter
public class RefreshTokenExpirationPolicyBuilder implements ExpirationPolicyBuilder<RefreshToken> {
    private static final long serialVersionUID = -3597980180617072826L;
    /**
     * The Cas properties.
     */
    protected final CasConfigurationProperties casProperties;

    @Override
    public ExpirationPolicy buildTicketExpirationPolicy() {
        return toTicketExpirationPolicy();
    }

    @Override
    public Class<RefreshToken> getTicketType() {
        return RefreshToken.class;
    }

    /**
     * To ticket expiration policy.
     *
     * @return the expiration policy
     */
    public ExpirationPolicy toTicketExpirationPolicy() {
        val rtProps = casProperties.getAuthn().getOauth().getRefreshToken();
        val timeout = Beans.newDuration(rtProps.getTimeToKillInSeconds()).getSeconds();
        if (casProperties.getLogout().isRemoveDescendantTickets()) {
            return new OAuthRefreshTokenExpirationPolicy(timeout);
        }
        return new OAuthRefreshTokenExpirationPolicy.OAuthRefreshTokenStandaloneExpirationPolicy(timeout);
    }
}
