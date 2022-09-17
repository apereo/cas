package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.Ticket;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link HazelcastTicketHolder}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Getter
@Setter
@SuperBuilder
public class HazelcastTicketHolder implements Serializable {
    @Serial
    private static final long serialVersionUID = -4741536838543052903L;

    private String id;

    private String type;

    private String principal;

    private Ticket ticket;

    private long timeToLive;
}
