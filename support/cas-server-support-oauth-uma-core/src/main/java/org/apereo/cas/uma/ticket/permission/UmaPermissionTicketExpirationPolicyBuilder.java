package org.apereo.cas.uma.ticket.permission;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * This is {@link UmaPermissionTicketExpirationPolicyBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@RequiredArgsConstructor
public class UmaPermissionTicketExpirationPolicyBuilder implements ExpirationPolicyBuilder<UmaPermissionTicket> {
    @Serial
    private static final long serialVersionUID = -3897980180617072826L;

    private final CasConfigurationProperties casProperties;

    @Override
    public ExpirationPolicy buildTicketExpirationPolicy() {
        return toTicketExpirationPolicy();
    }

    /**
     * To ticket expiration policy.
     *
     * @return the expiration policy
     */
    private ExpirationPolicy toTicketExpirationPolicy() {
        val uma = casProperties.getAuthn().getOauth().getUma();
        return new HardTimeoutExpirationPolicy(Beans.newDuration(uma.getPermissionTicket().getMaxTimeToLiveInSeconds()).toSeconds());
    }
}

