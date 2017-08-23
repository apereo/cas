package org.apereo.cas.ticket.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apereo.cas.ticket.support.HardTimeoutExpirationPolicy;

/**
 * This is {@link SamlAttributeQueryTicketExpirationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class SamlAttributeQueryTicketExpirationPolicy extends HardTimeoutExpirationPolicy {
    private static final long serialVersionUID = -6574724814970233926L;

    public SamlAttributeQueryTicketExpirationPolicy() {
    }

    @JsonCreator
    public SamlAttributeQueryTicketExpirationPolicy(@JsonProperty("timeToLive") final long timeToKillInSeconds) {
        super(timeToKillInSeconds);
    }
}
