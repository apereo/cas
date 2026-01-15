package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.ticket.Ticket;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * This is {@link HazelcastTicketDocument}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Getter
@Setter
@SuperBuilder
public class HazelcastTicketDocument implements Serializable {
    @Serial
    private static final long serialVersionUID = -4741536838543052903L;

    private String id;

    private String type;

    private String principal;

    private String service;

    private Ticket ticket;

    private long timeToLive;

    private String prefix;

    @Builder.Default
    private Map<String, List<Object>> attributes = new HashMap<>();
}
