package org.apereo.cas.oidc.ticket;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.expiration.MultiTimeUseOrTimeoutExpirationPolicy;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.ToString;
import lombok.val;

import java.io.Serial;

/**
 * This is {@link OidcPushedAuthorizationRequestExpirationPolicyBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public record OidcPushedAuthorizationRequestExpirationPolicyBuilder(CasConfigurationProperties casProperties) implements ExpirationPolicyBuilder<OidcPushedAuthorizationRequest> {
    @Serial
    private static final long serialVersionUID = -372536596516253646L;

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
        val par = casProperties.getAuthn().getOidc().getPar();
        val exp = Beans.newDuration(par.getMaxTimeToLiveInSeconds());
        return new OidcPushedAuthorizationRequestExpirationPolicy(par.getNumberOfUses(), exp.toSeconds());
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    @ToString(callSuper = true)
    public static class OidcPushedAuthorizationRequestExpirationPolicy extends MultiTimeUseOrTimeoutExpirationPolicy {
        @Serial
        private static final long serialVersionUID = -8814501080268311070L;

        @JsonCreator
        public OidcPushedAuthorizationRequestExpirationPolicy(
            @JsonProperty("numberOfUses")
            final long numberOfUses,
            @JsonProperty("timeToLive")
            final long timeToKillInSeconds) {
            super(numberOfUses, timeToKillInSeconds);
        }
    }
}
