package org.apereo.cas.ticket.registry;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.index.Indexed;

import java.io.Serializable;
import java.util.Date;

/**
 * This is {@link TicketHolder}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@Getter
@AllArgsConstructor
@ToString
public class TicketHolder implements Serializable {

    /**
     * Field name to hold ticket json data.
     */
    public static final String FIELD_NAME_JSON = "json";

    /**
     * Field name to hold ticket expiration time.
     */
    public static final String FIELD_NAME_EXPIRE_AT = "expireAt";

    /**
     * Field name to hold ticket id.
     */
    public static final String FIELD_NAME_ID = "ticketId";

    private static final long serialVersionUID = -4843440028617071224L;

    private final String json;

    private final String ticketId;

    private final String type;

    @Indexed
    private final Date expireAt;
}
