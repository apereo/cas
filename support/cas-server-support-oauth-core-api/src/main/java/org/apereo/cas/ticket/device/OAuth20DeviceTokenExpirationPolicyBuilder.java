package org.apereo.cas.ticket.device;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.val;

/**
 * This is {@link OAuth20DeviceTokenExpirationPolicyBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public record OAuth20DeviceTokenExpirationPolicyBuilder(CasConfigurationProperties casProperties) implements ExpirationPolicyBuilder<OAuth20DeviceToken> {
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
        val oauth = casProperties.getAuthn().getOauth().getDeviceToken();
        return new OAuth20DeviceTokenExpirationPolicy(Beans.newDuration(oauth.getMaxTimeToLiveInSeconds()).toSeconds());
    }
}
