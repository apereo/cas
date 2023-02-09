package org.apereo.cas.ticket.registry;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * This is {@link RedisTicketDocument}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Getter
@ToString
@Setter
@SuperBuilder
@NoArgsConstructor(force = true)
public class RedisTicketDocument implements Serializable {

    /**
     * Field name to hold ticket json data.
     */
    public static final String FIELD_NAME_JSON = "json";

    /**
     * Field name to hold ticket type.
     */
    public static final String FIELD_NAME_TYPE = "type";

    /**
     * Field name to hold ticket id.
     */
    public static final String FIELD_NAME_ID = "ticketId";

    /**
     * Field name to hold the principal id.
     */
    public static final String FIELD_NAME_PRINCIPAL = "principal";

    /**
     * Field name to hold the principal/authentication attributes.
     */
    public static final String FIELD_NAME_ATTRIBUTES = "attributes";

    @Serial
    private static final long serialVersionUID = -5043447728617071226L;

    @JsonProperty
    private String json;

    @JsonProperty
    private String ticketId;

    @JsonProperty
    private String type;

    @JsonProperty
    private String principal;

    @JsonProperty
    private Map<String, ?> attributes;
}
