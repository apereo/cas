package org.apereo.cas.ticket.refreshtoken;

import module java.base;
import org.apereo.cas.ticket.TicketGrantingTicketAwareTicket;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * An expiration policy that is independent from the parent ticket-granting ticket.
 * Activated when refresh tokens are expected to live beyond the normal expiration policy
 * of the {@code TGT} that lent a hand in issuing them. If the refresh token is considered expired
 * by this policy, the parent ticket's expiration policy is not consulted, making the RT independent.
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OAuth20RefreshTokenStandaloneExpirationPolicy extends OAuth20RefreshTokenExpirationPolicy {
    @Serial
    private static final long serialVersionUID = -7768661082888351104L;

    @JsonCreator
    public OAuth20RefreshTokenStandaloneExpirationPolicy(
        @JsonProperty("timeToLive") final long timeToKillInSeconds) {
        super(timeToKillInSeconds);
    }

    @Override
    public boolean isExpired(final TicketGrantingTicketAwareTicket ticketState) {
        return isRefreshTokenExpired(ticketState);
    }
}
