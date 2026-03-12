package org.apereo.cas.oidc.token;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * This is {@link OidcIdTokenExpirationPolicyBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@RequiredArgsConstructor
public class OidcIdTokenExpirationPolicyBuilder implements ExpirationPolicyBuilder<OAuth20AccessToken> {
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
        val timeout = Beans.newDuration(casProperties.getAuthn().getOidc().getIdToken().getMaxTimeToLiveInSeconds());
        return new HardTimeoutExpirationPolicy(timeout.toSeconds());
    }

}
