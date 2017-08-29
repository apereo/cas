package org.apereo.cas.ticket.registry;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.mongodb.core.index.Indexed;

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

    private final String ticketId;

    private final String type;

    @Indexed
    private final Date expireAt;

    public TicketHolder(final String json, final String ticketId,
                        final String type, final Date expireAt) {
        this.json = json;
        this.ticketId = ticketId;
        this.type = type;
        this.expireAt = new Date(expireAt.getTime());
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

    public Date getExpireAt() {
        return expireAt;
    }

}
