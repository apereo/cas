package org.apereo.cas.mfa.simple.ticket;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * This is {@link CasSimpleMultifactorAuthenticationTicketExpirationPolicyBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@RequiredArgsConstructor
public class CasSimpleMultifactorAuthenticationTicketExpirationPolicyBuilder implements ExpirationPolicyBuilder<TransientSessionTicket> {
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
        val simple = casProperties.getAuthn().getMfa().getSimple();
        return new HardTimeoutExpirationPolicy(simple.getToken().getCore().getTimeToKillInSeconds());
    }

}
