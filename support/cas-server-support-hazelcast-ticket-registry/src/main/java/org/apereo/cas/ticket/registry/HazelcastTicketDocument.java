package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.Ticket;
import lombok.Builder;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * This is {@link HazelcastTicketDocument}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Builder
public record HazelcastTicketDocument(String id, String type, String principal, String service,
    Ticket ticket, long timeToLive, String prefix, Map<String, List<Object>> attributes) implements Serializable {
    @Serial
    private static final long serialVersionUID = -4741536838543052903L;
}
