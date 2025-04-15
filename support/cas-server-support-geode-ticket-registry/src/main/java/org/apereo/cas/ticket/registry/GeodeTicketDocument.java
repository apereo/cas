package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.Ticket;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link GeodeTicketDocument}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Getter
@Setter
@SuperBuilder
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@EqualsAndHashCode
public class GeodeTicketDocument implements Serializable {
    @Serial
    private static final long serialVersionUID = -1742526828543052903L;

    private String id;

    private String kind;

    private String principal;

    private Ticket ticket;

    private String prefix;

    private String attributes;
}
