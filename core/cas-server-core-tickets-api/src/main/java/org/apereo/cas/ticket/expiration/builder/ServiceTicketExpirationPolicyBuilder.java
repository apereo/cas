package org.apereo.cas.ticket.expiration.builder;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.expiration.MultiTimeUseOrTimeoutExpirationPolicy;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.val;

/**
 * This is {@link ServiceTicketExpirationPolicyBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiredArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@ToString
@Getter
public class ServiceTicketExpirationPolicyBuilder implements ExpirationPolicyBuilder<ServiceTicket> {
    private static final long serialVersionUID = -3597980180617072826L;
    /**
     * The Cas properties.
     */
    protected final CasConfigurationProperties casProperties;

    @Override
    public ExpirationPolicy buildTicketExpirationPolicy() {
        return toServiceTicketExpirationPolicy();
    }

    @Override
    public Class<ServiceTicket> getTicketType() {
        return ServiceTicket.class;
    }

    /**
     * To service ticket expiration policy.
     *
     * @return the expiration policy
     */
    public ExpirationPolicy toServiceTicketExpirationPolicy() {
        val st = casProperties.getTicket().getSt();
        return new MultiTimeUseOrTimeoutExpirationPolicy.ServiceTicketExpirationPolicy(
            st.getNumberOfUses(),
            st.getTimeToKillInSeconds());
    }

}
