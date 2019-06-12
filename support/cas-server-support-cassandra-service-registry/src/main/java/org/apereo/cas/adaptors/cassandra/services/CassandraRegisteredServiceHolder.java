package org.apereo.cas.adaptors.cassandra.services;

import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import static org.apereo.cas.adaptors.cassandra.services.CassandraRegisteredServiceHolder.TABLE_NAME;

/**
 * This is {@link CassandraRegisteredServiceHolder}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@Table(name = TABLE_NAME, writeConsistency = "LOCAL_QUORUM", readConsistency = "ONE")
public class CassandraRegisteredServiceHolder {
    /**
     * Ticket table name.
     */
    public static final String TABLE_NAME = "casservices";

    /**
     * The Id.
     */
    @PartitionKey
    private long id;

    /**
     * The Data.
     */
    private String data;

    @JsonCreator
    public CassandraRegisteredServiceHolder(@JsonProperty("id") final long id, @JsonProperty("data") final String data) {
        this.id = id;
        this.data = data;
    }
}
