package org.apereo.cas.ticket.query;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.artifact.SamlArtifactTicket;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * This is {@link SamlAttributeQueryTicketExpirationPolicyBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiredArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@ToString
@Getter
public class SamlAttributeQueryTicketExpirationPolicyBuilder implements ExpirationPolicyBuilder<SamlArtifactTicket> {
    private static final long serialVersionUID = -3597980180617072826L;
    /**
     * The Cas properties.
     */
    protected final CasConfigurationProperties casProperties;

    @Override
    public ExpirationPolicy buildTicketExpirationPolicy() {
        return toTicketExpirationPolicy();
    }

    @Override
    public Class<SamlArtifactTicket> getTicketType() {
        return SamlArtifactTicket.class;
    }

    /**
     * To ticket expiration policy.
     *
     * @return the expiration policy
     */
    public ExpirationPolicy toTicketExpirationPolicy() {
        return new SamlAttributeQueryTicketExpirationPolicy(casProperties.getTicket().getSt().getTimeToKillInSeconds());
    }
}

