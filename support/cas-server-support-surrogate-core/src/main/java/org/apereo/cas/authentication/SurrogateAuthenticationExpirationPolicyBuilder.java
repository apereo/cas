package org.apereo.cas.authentication;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.expiration.BaseDelegatingExpirationPolicy;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.expiration.SurrogateSessionExpirationPolicy;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.val;

import java.io.Serial;

/**
 * This is {@link SurrogateAuthenticationExpirationPolicyBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public record SurrogateAuthenticationExpirationPolicyBuilder(ExpirationPolicyBuilder<TicketGrantingTicket> ticketGrantingTicketExpirationPolicyBuilder,
                                                             CasConfigurationProperties casProperties) implements ExpirationPolicyBuilder<TicketGrantingTicket> {
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
        val su = casProperties.getAuthn().getSurrogate();
        val surrogatePolicy = new HardTimeoutExpirationPolicy(su.getTgt().getTimeToKillInSeconds());
        val policy = new SurrogateSessionExpirationPolicy();
        policy.addPolicy(SurrogateSessionExpirationPolicy.POLICY_NAME_SURROGATE, surrogatePolicy);
        policy.addPolicy(BaseDelegatingExpirationPolicy.POLICY_NAME_DEFAULT,
            ticketGrantingTicketExpirationPolicyBuilder.buildTicketExpirationPolicy());
        return policy;
    }
}
