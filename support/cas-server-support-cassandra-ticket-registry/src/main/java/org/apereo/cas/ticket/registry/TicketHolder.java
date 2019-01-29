package org.apereo.cas.ticket.registry;

import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * This is {@link TicketHolder}
 *
 * @since 6.1.0
 */
@Table(name = "ticket", writeConsistency = "LOCAL_QUORUM", readConsistency = "ONE")
public class TicketHolder {

    @PartitionKey
    private String id;
    private String data;

    @JsonCreator
    public TicketHolder(@JsonProperty("id") final String id, @JsonProperty("data") final String data) {
        this.id = id;
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public String getData() {
        return data;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TicketHolder)) {
            return false;
        }
        var that = (TicketHolder) o;
        return Objects.equals(id, that.id)
                && Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, data);
    }
}
