package org.apereo.cas.ticket;

import org.apereo.cas.util.NamedObject;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

/**
 * The {@link TicketFactory} is an abstraction that decides
 * how CAS ticket factory objects are created.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public interface TicketFactory extends NamedObject {

    /**
     * Default implementation bean name.
     */
    String BEAN_NAME = "defaultTicketFactory";

    /**
     * Get ticket factory.
     *
     * @param clazz the clazz
     * @return ticket factory object
     */
    @CanIgnoreReturnValue
    default TicketFactory get(final Class<? extends Ticket> clazz) {
        return this;
    }

    /**
     * Gets ticket type.
     *
     * @return the ticket type
     */
    Class<? extends Ticket> getTicketType();

    /**
     * Gets expiration policy builder.
     *
     * @return the expiration policy builder
     */
    ExpirationPolicyBuilder getExpirationPolicyBuilder();

    /**
     * Gets ticket id generator.
     *
     * @return the ticket id generator
     */
    default UniqueTicketIdGenerator getTicketIdGenerator() {
        return UniqueTicketIdGenerator.prefixedTicketIdGenerator();
    }
}
