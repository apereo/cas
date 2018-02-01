package org.apereo.cas.ticket.support;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.ticket.TicketState;
import lombok.NoArgsConstructor;

/**
 * AlwaysExpiresExpirationPolicy always answers true when asked if a Ticket is
 * expired.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
@Slf4j
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AlwaysExpiresExpirationPolicy extends AbstractCasExpirationPolicy {

    /**
     * Serializable Unique ID.
     */
    private static final long serialVersionUID = 3836547698242303540L;

    @Override
    public boolean isExpired(final TicketState ticketState) {
        return true;
    }

    @JsonIgnore
    @Override
    public Long getTimeToLive() {
        return 0L;
    }

    @JsonIgnore
    @Override
    public Long getTimeToIdle() {
        return 0L;
    }

}
