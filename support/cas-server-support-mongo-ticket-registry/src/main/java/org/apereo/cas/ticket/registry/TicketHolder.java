package org.apereo.cas.ticket.registry;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
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
@ToString
@Document
@Setter
@SuperBuilder
@NoArgsConstructor(force = true)
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

    /**
     * Field name to hold the principal id.
     */
    public static final String FIELD_NAME_PRINCIPAL = "principal";

    private static final long serialVersionUID = -4843440028617071224L;

    @JsonProperty
    private String json;

    @JsonProperty
    private String ticketId;

    @JsonProperty
    private String type;

    @JsonProperty
    private String principal;

    private Date expireAt;
}
