package org.apereo.cas.ticket.artifact;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.ticket.support.HardTimeoutExpirationPolicy;
import lombok.NoArgsConstructor;

/**
 * This is {@link SamlArtifactTicketExpirationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
@Slf4j
@NoArgsConstructor
public class SamlArtifactTicketExpirationPolicy extends HardTimeoutExpirationPolicy {

    private static final long serialVersionUID = -6574724814970233926L;

    @JsonCreator
    public SamlArtifactTicketExpirationPolicy(@JsonProperty("timeToLive") final long timeToKillInSeconds) {
        super(timeToKillInSeconds);
    }
}
