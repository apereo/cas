package org.apereo.cas.ticket.artifact;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apereo.cas.ticket.support.HardTimeoutExpirationPolicy;

/**
 * This is {@link SamlArtifactTicketExpirationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class SamlArtifactTicketExpirationPolicy extends HardTimeoutExpirationPolicy {
    private static final long serialVersionUID = -6574724814970233926L;

    public SamlArtifactTicketExpirationPolicy() {
    }

    @JsonCreator
    public SamlArtifactTicketExpirationPolicy(@JsonProperty("timeToLive") final long timeToKillInSeconds) {
        super(timeToKillInSeconds);
    }
}
