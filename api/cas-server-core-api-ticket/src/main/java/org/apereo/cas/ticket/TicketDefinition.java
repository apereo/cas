package org.apereo.cas.ticket;

import org.springframework.core.Ordered;

/**
 * This is {@link TicketDefinition}. Ticket definition describes additional Properties and misc settings
 * that may be associated with a given ticket to be used by registries. Each CAS module on start up
 * has the ability to register/alter ticket metadata that may be requires for its own specific functionality.
 * Given each CAS module may decide to create many forms of tickets, this facility is specifically provided
 * to dynamically register ticket types and associated properties so modules that deal with registry functionality
 * wouldn't have to statically link to all modules and APIs.
 *
 * @author Misagh Moayyed
 * @see TicketCatalog
 * @since 5.1.0
 */
public interface TicketDefinition extends Ordered {
    /**
     * Gets prefix.
     *
     * @return the prefix
     */
    String getPrefix();

    /**
     * Gets implementation class.
     *
     * @return the implementation class
     */
    Class<? extends Ticket> getImplementationClass();

    /**
     * Gets properties.
     *
     * @return the properties
     */
    TicketDefinitionProperties getProperties();

    /**
     * Returns order/priority associated with this definition.
     * Typically used in collection sorting and compare operations.
     * @return the order.
     */
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
