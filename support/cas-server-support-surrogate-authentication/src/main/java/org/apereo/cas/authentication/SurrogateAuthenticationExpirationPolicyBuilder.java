package org.apereo.cas.authentication;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.expiration.SurrogateSessionExpirationPolicy;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.val;

/**
 * This is {@link SurrogateAuthenticationExpirationPolicyBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiredArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@ToString
@Getter
public class SurrogateAuthenticationExpirationPolicyBuilder implements ExpirationPolicyBuilder<TicketGrantingTicket> {
    private static final long serialVersionUID = -3597980180617072826L;

    /**
     * The Ticket granting ticket expiration policy builder.
     */
    protected final ExpirationPolicyBuilder<TicketGrantingTicket> ticketGrantingTicketExpirationPolicyBuilder;

    /**
     * The Cas properties.
     */
    protected final CasConfigurationProperties casProperties;

    @Override
    public ExpirationPolicy buildTicketExpirationPolicy() {
        return toTicketExpirationPolicy();
    }

    @Override
    public Class<TicketGrantingTicket> getTicketType() {
        return TicketGrantingTicket.class;
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
        policy.addPolicy(SurrogateSessionExpirationPolicy.POLICY_NAME_DEFAULT,
            ticketGrantingTicketExpirationPolicyBuilder.buildTicketExpirationPolicy());
        return policy;
    }
}
