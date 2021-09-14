package org.apereo.cas.ticket.expiration.builder;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.expiration.MultiTimeUseOrTimeoutExpirationPolicy;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.val;

/**
 * This is {@link TransientSessionTicketExpirationPolicyBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiredArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@ToString
@Getter
public class TransientSessionTicketExpirationPolicyBuilder implements ExpirationPolicyBuilder<TransientSessionTicket> {
    private static final long serialVersionUID = -1587980180617072826L;

    /**
     * The Cas properties.
     */
    protected final CasConfigurationProperties casProperties;

    @Override
    public ExpirationPolicy buildTicketExpirationPolicy() {
        return toTransientSessionTicketExpirationPolicy();
    }

    @Override
    public Class<TransientSessionTicket> getTicketType() {
        return TransientSessionTicket.class;
    }

    /**
     * To transient ticket expiration policy.
     *
     * @return the expiration policy
     */
    public ExpirationPolicy toTransientSessionTicketExpirationPolicy() {
        val tst = casProperties.getTicket().getTst();
        return new MultiTimeUseOrTimeoutExpirationPolicy.TransientSessionTicketExpirationPolicy(
            tst.getNumberOfUses(),
            tst.getTimeToKillInSeconds());
    }
}
