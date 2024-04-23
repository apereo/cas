package org.apereo.cas.ticket.expiration.builder;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.expiration.MultiTimeUseOrTimeoutExpirationPolicy;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.val;

import java.io.Serial;

/**
 * This is {@link ServiceTicketExpirationPolicyBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public record ServiceTicketExpirationPolicyBuilder(CasConfigurationProperties casProperties) implements ExpirationPolicyBuilder<ServiceTicket> {
    @Serial
    private static final long serialVersionUID = -3597980180617072826L;

    @Override
    public ExpirationPolicy buildTicketExpirationPolicy() {
        return toServiceTicketExpirationPolicy();
    }

    /**
     * To service ticket expiration policy.
     *
     * @return the expiration policy
     */
    public ExpirationPolicy toServiceTicketExpirationPolicy() {
        val st = casProperties.getTicket().getSt();
        val timeToKillInSeconds = Beans.newDuration(st.getTimeToKillInSeconds()).toSeconds();
        return new MultiTimeUseOrTimeoutExpirationPolicy.ServiceTicketExpirationPolicy(st.getNumberOfUses(), timeToKillInSeconds);
    }

}
