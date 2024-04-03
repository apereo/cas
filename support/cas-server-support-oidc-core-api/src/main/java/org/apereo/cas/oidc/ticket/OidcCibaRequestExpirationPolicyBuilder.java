package org.apereo.cas.oidc.ticket;

import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serial;
import java.time.Duration;

/**
 * This is {@link OidcCibaRequestExpirationPolicyBuilder}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public class OidcCibaRequestExpirationPolicyBuilder implements ExpirationPolicyBuilder<OidcPushedAuthorizationRequest> {
    @Serial
    private static final long serialVersionUID = -371536596516253646L;

    @Override
    public ExpirationPolicy buildTicketExpirationPolicy() {
        return new HardTimeoutExpirationPolicy(Duration.ofMinutes(5).toSeconds());
    }
}
