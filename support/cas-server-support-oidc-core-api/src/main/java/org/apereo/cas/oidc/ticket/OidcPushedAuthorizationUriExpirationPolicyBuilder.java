package org.apereo.cas.oidc.ticket;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.val;

/**
 * This is {@link OidcPushedAuthorizationUriExpirationPolicyBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiredArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@ToString
@Getter
public class OidcPushedAuthorizationUriExpirationPolicyBuilder implements ExpirationPolicyBuilder<OidcPushedAuthorizationRequest> {
    private static final long serialVersionUID = -372536596516253646L;

    /**
     * The CAS properties.
     */
    protected final CasConfigurationProperties casProperties;

    @Override
    public ExpirationPolicy buildTicketExpirationPolicy() {
        return toTicketExpirationPolicy();
    }

    @Override
    public Class<OidcPushedAuthorizationRequest> getTicketType() {
        return OidcPushedAuthorizationRequest.class;
    }

    /**
     * To ticket expiration policy.
     *
     * @return the expiration policy
     */
    public ExpirationPolicy toTicketExpirationPolicy() {
        val exp = Beans.newDuration(casProperties.getAuthn().getOidc().getPar().getMaxTimeToLiveInSeconds());
        return new HardTimeoutExpirationPolicy(exp.getSeconds());
    }
}
