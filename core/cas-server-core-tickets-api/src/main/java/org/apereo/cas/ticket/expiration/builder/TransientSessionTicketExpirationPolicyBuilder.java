package org.apereo.cas.ticket.expiration.builder;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.expiration.MultiTimeUseOrTimeoutExpirationPolicy;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.val;

/**
 * This is {@link TransientSessionTicketExpirationPolicyBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public record TransientSessionTicketExpirationPolicyBuilder(CasConfigurationProperties casProperties) implements ExpirationPolicyBuilder<TransientSessionTicket> {
    @Serial
    private static final long serialVersionUID = -1587980180617072826L;

    @Override
    public ExpirationPolicy buildTicketExpirationPolicy() {
        return toTransientSessionTicketExpirationPolicy();
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
