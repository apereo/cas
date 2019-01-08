package org.apereo.cas.ticket.registry;

import java.util.Objects;

import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Table(name = "ticket", writeConsistency = "LOCAL_QUORUM", readConsistency = "ONE")
public class TicketHolder {

    @PartitionKey
    private String id;
    private String data;

    @JsonCreator
    public TicketHolder(@JsonProperty("id") String id, @JsonProperty("data") String data) {
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof TicketHolder)) {
            return false;
        }
        TicketHolder that = (TicketHolder) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, data);
    }
}