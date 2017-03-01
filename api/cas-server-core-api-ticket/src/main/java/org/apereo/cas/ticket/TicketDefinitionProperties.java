package org.apereo.cas.ticket;

/**
 * This is {@link TicketDefinitionProperties}. Ticket definition describes additional Properties and misc settings
 * that may be associated with a given ticket to be used by registries. Each CAS module on start up
 * has the ability to register/alter ticket metadata that may be requires for its own specific functionality.
 * Given each CAS module may decide to create many forms of tickets, this facility is specifically provided
 * to dynamically register ticket types and associated properties so modules that deal with registry functionality
 * wouldn't have to statically link to all modules and APIs.
 *
 * @author Misagh Moayyed
 * @see TicketMetadataCatalog
 * @since 5.1.0
 */
public interface TicketDefinitionProperties {

    /**
     * Generically describes if this ticket is linked to all ticket entities
     * such that for normal CRUD operations, cascades may be required.
     *
     * @return true/false
     */
    boolean isCascade();

    /**
     * Sets cascade ticket.
     *
     * @param cascadeTicket the cascade ticket
     */
    void setCascade(boolean cascadeTicket);

    /**
     * Generic cache name this ticket may want to associate with itself
     * in cases where persistence is handled by an underlying cache, etc.
     *
     * @return the cache name
     */
    String getCacheName();

    /**
     * Sets cache name.
     *
     * @param cacheName the cache name
     */
    void setCacheName(String cacheName);
}
