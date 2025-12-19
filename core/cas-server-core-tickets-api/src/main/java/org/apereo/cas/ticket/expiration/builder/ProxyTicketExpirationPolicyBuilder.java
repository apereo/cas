package org.apereo.cas.ticket.expiration.builder;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.expiration.MultiTimeUseOrTimeoutExpirationPolicy;
import org.apereo.cas.ticket.proxy.ProxyTicket;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.val;

/**
 * This is {@link ProxyTicketExpirationPolicyBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public record ProxyTicketExpirationPolicyBuilder(CasConfigurationProperties casProperties) implements ExpirationPolicyBuilder<ProxyTicket> {
    @Serial
    private static final long serialVersionUID = -2597980180617072826L;

    @Override
    public ExpirationPolicy buildTicketExpirationPolicy() {
        return toProxyTicketExpirationPolicy();
    }

    /**
     * To proxy ticket expiration policy.
     *
     * @return the expiration policy
     */
    public ExpirationPolicy toProxyTicketExpirationPolicy() {
        val pt = casProperties.getTicket().getPt();
        return new MultiTimeUseOrTimeoutExpirationPolicy.ProxyTicketExpirationPolicy(
            pt.getNumberOfUses(),
            pt.getTimeToKillInSeconds());
    }

}
