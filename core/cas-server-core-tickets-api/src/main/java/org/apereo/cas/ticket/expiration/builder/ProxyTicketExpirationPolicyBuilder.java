package org.apereo.cas.ticket.expiration.builder;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.expiration.MultiTimeUseOrTimeoutExpirationPolicy;
import org.apereo.cas.ticket.proxy.ProxyTicket;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.val;

/**
 * This is {@link ProxyTicketExpirationPolicyBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiredArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@ToString
@Getter
public class ProxyTicketExpirationPolicyBuilder implements ExpirationPolicyBuilder<ProxyTicket> {
    private static final long serialVersionUID = -2597980180617072826L;
    /**
     * The Cas properties.
     */
    protected final CasConfigurationProperties casProperties;

    @Override
    public ExpirationPolicy buildTicketExpirationPolicy() {
        return toProxyTicketExpirationPolicy();
    }

    @Override
    public Class<ProxyTicket> getTicketType() {
        return ProxyTicket.class;
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
