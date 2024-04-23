package org.apereo.cas.ticket;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

/**
 * The {@link TicketFactory} is an abstraction that decides
 * how CAS ticket factory objects are created.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public interface TicketFactory {

    /**
     * Default implementation bean name.
     */
    String BEAN_NAME = "defaultTicketFactory";

    /**
     * Gets name.
     *
     * @return the name
     */
    default String getName() {
        return getClass().getSimpleName();
    }

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
}
