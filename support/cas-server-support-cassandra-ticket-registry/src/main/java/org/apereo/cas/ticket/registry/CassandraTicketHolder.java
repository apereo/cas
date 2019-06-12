package org.apereo.cas.ticket.registry;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * This is {@link CassandraTicketHolder}.
 *
 * @author Misagh Moayyed
 * @author doomviking
 * @since 6.1.0
 */
@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class CassandraTicketHolder implements Serializable {
    private static final long serialVersionUID = -4308217682209741077L;

    /**
     * The Id.
     */
    private String id;

    /**
     * The Data.
     */
    private String data;

    /**
     * Ticket type.
     */
    private String type;

    @JsonCreator
    public CassandraTicketHolder(@JsonProperty("id") final String id, @JsonProperty("data") final String data,
                                 @JsonProperty("type") final String type) {
        this.id = id;
        this.data = data;
        this.type = type;
    }
}
