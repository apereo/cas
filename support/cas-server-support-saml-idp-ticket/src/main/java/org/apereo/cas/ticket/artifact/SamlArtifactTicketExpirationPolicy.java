package org.apereo.cas.ticket.artifact;

import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * This is {@link SamlArtifactTicketExpirationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@NoArgsConstructor
@ToString(callSuper = true)
public class SamlArtifactTicketExpirationPolicy extends HardTimeoutExpirationPolicy {

    private static final long serialVersionUID = -6574724814970233926L;

    @JsonCreator
    public SamlArtifactTicketExpirationPolicy(@JsonProperty("timeToLive") final long timeToKillInSeconds) {
        super(timeToKillInSeconds);
    }
}
