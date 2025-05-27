package org.apereo.cas.ticket;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

/**
 * Interface for the generic concept of a ticket.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface Ticket extends ExpirableTicket, StatelessTicket, Comparable<Ticket> {

    /**
     * Method to retrieve the id.
     *
     * @return the id
     */
    String getId();

    /**
     * Method to retrieve the tenant id that owns and issued this ticket.
     * @return tenant id or null
     */
    String getTenantId();

    /**
     * Gets count of uses.
     *
     * @return the number of times this ticket was used.
     */
    default int getCountOfUses() {
        return 0;
    }

    /**
     * Gets prefix.
     *
     * @return the prefix
     */
    String getPrefix();

    /**
     * May record the <i>previous</i> last time this ticket was used as well as
     * the last usage time. The ticket usage count is also incremented.
     * <p>Tickets themselves are solely responsible to maintain their state. The
     * determination of ticket usage is left up to the implementation and
     * the specific ticket type.
     *
     * @return the ticket
     * @see ExpirationPolicy
     * @since 5.0.0
     */
    @CanIgnoreReturnValue
    default Ticket update() {
        return this;
    }
}
