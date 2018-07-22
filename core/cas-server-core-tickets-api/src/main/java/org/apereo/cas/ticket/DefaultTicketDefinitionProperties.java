package org.apereo.cas.ticket;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * This is {@link DefaultTicketDefinitionProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@ToString
@Getter
@EqualsAndHashCode
@Setter
public class DefaultTicketDefinitionProperties implements TicketDefinitionProperties {

    /**
     * Whether ticket operations require cascading down in the storage.
     */
    private boolean cascade;

    /**
     * Storage/cache name that holds this ticket.
     */
    private String storageName;

    /**
     * Timeout for this ticket.
     */
    private long storageTimeout;

    /**
     * Password for this ticket storage, if any.
     */
    private String storagePassword;
}
