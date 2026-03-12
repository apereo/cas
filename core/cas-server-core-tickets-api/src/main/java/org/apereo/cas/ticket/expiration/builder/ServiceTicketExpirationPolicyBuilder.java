package org.apereo.cas.ticket.expiration.builder;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.expiration.MultiTimeUseOrTimeoutExpirationPolicy;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * This is {@link ServiceTicketExpirationPolicyBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@RequiredArgsConstructor
public class ServiceTicketExpirationPolicyBuilder implements ExpirationPolicyBuilder<ServiceTicket> {
    @Serial
    private static final long serialVersionUID = -3597980180617072826L;
    private final CasConfigurationProperties casProperties;

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
