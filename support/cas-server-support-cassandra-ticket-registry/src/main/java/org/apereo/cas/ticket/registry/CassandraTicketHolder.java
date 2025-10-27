package org.apereo.cas.ticket.registry;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

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
@AllArgsConstructor
@SuperBuilder
@Setter
public class CassandraTicketHolder implements Serializable {
    @Serial
    private static final long serialVersionUID = -4308217682209741077L;

    @JsonProperty("id")
    private String id;

    @JsonProperty("principal")
    private String principal;

    @JsonProperty("data")
    private String data;

    @JsonProperty("type")
    private String type;

    @JsonProperty("prefix")
    private String prefix;

    @JsonProperty("attributes")
    private Map<String, String> attributes;
}
