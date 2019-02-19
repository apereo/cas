package org.apereo.cas.ticket.registry;

import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import static org.apereo.cas.ticket.registry.CassandraTicketHolder.TABLE_NAME;

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
@Table(name = TABLE_NAME, writeConsistency = "LOCAL_QUORUM", readConsistency = "ONE")
public class CassandraTicketHolder {

    /**
     * Ticket table name.
     */
    public static final String TABLE_NAME = "castickets";

    /**
     * The Id.
     */
    @PartitionKey
    private final String id;

    /**
     * The Data.
     */
    private final String data;

    @JsonCreator
    public CassandraTicketHolder(@JsonProperty("id") final String id, @JsonProperty("data") final String data) {
        this.id = id;
        this.data = data;
    }
}
