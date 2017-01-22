package org.apereo.cas.ticket.registry;

import org.springframework.data.mongodb.core.index.Indexed;

import java.io.Serializable;

/**
 * This is {@link TicketHolder}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class TicketHolder implements Serializable {
    /** Field name to hold ticket json data. */
    public static final String FIELD_NAME_JSON = "json";

    /** Field name to hold ticket expiration time. */
    public static final String FIELD_NAME_EXPIRE_AT = "expireAt";

    /** Field name to hold ticket id. */
    public static final String FIELD_NAME_ID= "ticketId";
    
    private static final long serialVersionUID = -4843440028617071224L;
    
    private final String json;

    private String ticketId;

    private String type;

    @Indexed
    private final long expireAt;

    public TicketHolder(final String json, final String ticketId,
                        final String type, final long expireAt) {
        this.json = json;
        this.ticketId = ticketId;
        this.type = type;
        this.expireAt = expireAt;
    }

    public String getJson() {
        return json;
    }

    public String getTicketId() {
        return ticketId;
    }

    public String getType() {
        return type;
    }

    public long getExpireAt() {
        return expireAt;
    }

}
