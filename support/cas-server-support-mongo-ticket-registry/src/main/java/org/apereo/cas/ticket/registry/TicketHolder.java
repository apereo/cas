package org.apereo.cas.ticket.registry;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

/**
 * This is {@link TicketHolder}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Getter
@AllArgsConstructor
@ToString
@Document
@Setter
public class TicketHolder implements Serializable {

    /**
     * Field name to hold ticket json data.
     */
    public static final String FIELD_NAME_JSON = "json";

    /**
     * Field name to hold ticket type.
     */
    public static final String FIELD_NAME_TYPE = "type";

    /**
     * Field name to hold ticket expiration time.
     */
    public static final String FIELD_NAME_EXPIRE_AT = "expireAt";

    /**
     * Field name to hold ticket id.
     */
    public static final String FIELD_NAME_ID = "ticketId";

    private static final long serialVersionUID = -4843440028617071224L;

    @JsonProperty
    private final String json;

    @JsonProperty
    private final String ticketId;

    @JsonProperty
    private final String type;

    private final Date expireAt;
}
