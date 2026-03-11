package org.apereo.cas.ticket.device;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * This is {@link OAuth20DeviceTokenExpirationPolicyBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@RequiredArgsConstructor
public class OAuth20DeviceTokenExpirationPolicyBuilder implements ExpirationPolicyBuilder<OAuth20DeviceToken> {
    @Serial
    private static final long serialVersionUID = -3597980180617072826L;

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
        val oauth = casProperties.getAuthn().getOauth().getDeviceToken();
        return new OAuth20DeviceTokenExpirationPolicy(Beans.newDuration(oauth.getMaxTimeToLiveInSeconds()).toSeconds());
    }
}
