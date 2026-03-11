package org.apereo.cas.ticket.artifact;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * This is {@link SamlArtifactTicketExpirationPolicyBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@RequiredArgsConstructor
public class SamlArtifactTicketExpirationPolicyBuilder implements ExpirationPolicyBuilder<SamlArtifactTicket> {
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
        val timeToKillInSeconds = Beans.newDuration(casProperties.getTicket().getSt().getTimeToKillInSeconds()).toSeconds();
        return new SamlArtifactTicketExpirationPolicy(timeToKillInSeconds);
    }

}

