package org.apereo.cas.ticket.registry;

import module java.base;
import lombok.Builder;
import org.springframework.data.annotation.Id;

/**
 * This is {@link RedisTicketDocument}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Builder
public record RedisTicketDocument(String json, @Id String ticketId, String type,
    String principal, String prefix, String service, String attributes) implements Serializable {

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
     * Field name to hold the service/application URL, if any.
     */
    public static final String FIELD_NAME_SERVICE = "service";

    /**
     * Field name to hold the principal/authentication attribute names.
     */
    public static final String FIELD_NAME_ATTRIBUTES = "attributes";

    @Serial
    private static final long serialVersionUID = -5043447728617071226L;

    /**
     * From document map to redis document.
     *
     * @param document the document
     * @return the redis ticket document
     */
    public static RedisTicketDocument from(final Map<String, String> document) {
        return RedisTicketDocument
            .builder()
            .type(document.get(FIELD_NAME_TYPE))
            .ticketId(document.get(FIELD_NAME_ID))
            .json(document.get(FIELD_NAME_JSON))
            .prefix(document.get(FIELD_NAME_PREFIX))
            .principal(document.get(FIELD_NAME_PRINCIPAL))
            .attributes(document.get(FIELD_NAME_ATTRIBUTES))
            .service(document.get(FIELD_NAME_SERVICE))
            .build();
    }
}
