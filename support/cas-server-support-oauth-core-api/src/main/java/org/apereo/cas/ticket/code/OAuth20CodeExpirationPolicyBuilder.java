package org.apereo.cas.ticket.code;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * This is {@link OAuth20CodeExpirationPolicyBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@RequiredArgsConstructor
public class OAuth20CodeExpirationPolicyBuilder implements ExpirationPolicyBuilder<OAuth20Code> {
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
        val oauth = casProperties.getAuthn().getOauth();
        return new OAuth20CodeExpirationPolicy(oauth.getCode().getNumberOfUses(),
            oauth.getCode().getTimeToKillInSeconds());
    }
}
