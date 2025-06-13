package org.apereo.cas.ticket;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.io.Serial;

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

    @Serial
    private static final long serialVersionUID = -5634472680723687729L;
    /**
     * Whether ticket operations require cascading down in the storage.
     */
    private boolean cascadeRemovals;

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

    /**
     * If a ticket definition is going to be removed
     * as part of a cascade operation, should this definition
     * be excluded from removals allowing the ticket
     * to hang around without its parent?
     */
    private boolean excludeFromCascade;
}
