package org.apereo.cas.ticket.registry;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;

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
@Data
@Accessors(chain = true)
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
     * Field name to hold ticket prefix.
     */
    public static final String FIELD_NAME_PREFIX = "prefix";

    /**
     * Field name to hold the principal id.
     */
    public static final String FIELD_NAME_PRINCIPAL = "principal";

    /**
     * Field name to hold the principal/authentication attribute names.
     */
    public static final String FIELD_NAME_ATTRIBUTES = "attributes";

    @Serial
    private static final long serialVersionUID = -5043447728617071226L;
    
    @JsonProperty
    private String json;

    @JsonProperty
    @Id
    private String ticketId;

    @JsonProperty
    private String type;

    @JsonProperty
    private String principal;

    @JsonProperty
    private String prefix;

    @JsonProperty
    private String attributes;

    /**
     * From document map to redis document.
     *
     * @param document the document
     * @return the redis ticket document
     */
    public static RedisTicketDocument from(final Map<String, String> document) {
        return RedisTicketDocument.builder()
            .type(document.get(FIELD_NAME_TYPE))
            .ticketId(document.get(FIELD_NAME_ID))
            .json(document.get(FIELD_NAME_JSON))
            .prefix(document.get(FIELD_NAME_PREFIX))
            .principal(document.get(FIELD_NAME_PRINCIPAL))
            .attributes(document.get(FIELD_NAME_ATTRIBUTES))
            .build();
    }
}
