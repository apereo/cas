package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.Ticket;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link IgniteTicketDocument}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Getter
@Setter
@SuperBuilder
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@EqualsAndHashCode
public class IgniteTicketDocument implements Serializable {
    @Serial
    private static final long serialVersionUID = -4742526828543052903L;

    @QuerySqlField(index = true)
    private String id;

    @QuerySqlField(index = true)
    private String type;

    @QuerySqlField(index = true)
    private String principal;

    private Ticket ticket;

    @QuerySqlField(index = true)
    private String prefix;

    @QuerySqlField(index = true)
    private String attributes;
}
