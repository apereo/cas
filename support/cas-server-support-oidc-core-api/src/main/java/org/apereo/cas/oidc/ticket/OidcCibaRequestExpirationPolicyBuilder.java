package org.apereo.cas.oidc.ticket;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.RequiredArgsConstructor;
import lombok.val;
import java.io.Serial;

/**
 * This is {@link OidcCibaRequestExpirationPolicyBuilder}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@RequiredArgsConstructor
public class OidcCibaRequestExpirationPolicyBuilder implements ExpirationPolicyBuilder<OidcPushedAuthorizationRequest> {
    @Serial
    private static final long serialVersionUID = -371536596516253646L;

    private final CasConfigurationProperties casProperties;

    @Override
    public ExpirationPolicy buildTicketExpirationPolicy() {
        val seconds = Beans.newDuration(casProperties.getAuthn().getOidc().getCiba().getMaxTimeToLiveInSeconds()).toSeconds();
        return new HardTimeoutExpirationPolicy(seconds);
    }
}
