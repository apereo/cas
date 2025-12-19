package org.apereo.cas.ticket.query;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.val;

/**
 * This is {@link SamlAttributeQueryTicketExpirationPolicyBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public record SamlAttributeQueryTicketExpirationPolicyBuilder(CasConfigurationProperties casProperties) implements ExpirationPolicyBuilder<SamlAttributeQueryTicket> {
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
        val timeToKillInSeconds = casProperties.getAuthn().getSamlIdp()
            .getTicket().getAttributeQuery().getTimeToKillInSeconds();
        return timeToKillInSeconds <= 0
            ? new NeverExpiresExpirationPolicy()
            : new HardTimeoutExpirationPolicy(timeToKillInSeconds);
    }
}

