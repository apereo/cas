package org.apereo.cas.ticket;

/**
 * This is {@link TicketDefinitionProperties}. Ticket definition describes additional Properties and misc settings
 * that may be associated with a given ticket to be used by registries. Each CAS module on start up
 * has the ability to register/alter ticket metadata that may be requires for its own specific functionality.
 * Given each CAS module may decide to create many forms of tickets, this facility is specifically provided
 * to dynamically register ticket types and associated properties so modules that deal with registry functionality
 * wouldn't have to statically link to all modules and APIs.
 *
 * Ticket definition properties are intended to be treated as generally as possible, so common settings
 * can be shared across all modules that may have similar needs. When adding additional properties, be careful
 * to not tie the setting to a specific technology or terminology, and opt for generality as much as possible.
 * 
 * @author Misagh Moayyed
 * @see TicketCatalog
 * @since 5.1.0
 */
public interface TicketDefinitionProperties {

    /**
     * Generically describes if this ticket is linked to all ticket entities
     * such that for normal CRUD operations, cascades may be required.
     *
     * @return true /false
     */
    boolean isCascade();

    /**
     * Sets cascade ticket.
     *
     * @param cascadeTicket the cascade ticket
     */
    void setCascade(boolean cascadeTicket);

    /**
     * Generic cache/storage name this ticket may want to associate with itself
     * in cases where persistence is handled by an underlying cache, etc.
     *
     * @return the cache name
     */
    String getStorageName();

    /**
     * Sets store name.
     *
     * @param storageName the cache name
     */
    void setStorageName(String storageName);

    /**
     * Describes how long may this ticket definition
     * exist in the underlying storage unit. For cache-based storage
     * services, this may translate to idle/max time-to-live, etc.
     * @return the long
     */
    long getStorageTimeout();

    /**
     * Sets store password if any.
     *
     * @param psw the password for the storage.
     */
    void setStoragePassword(String psw);

    /**
     * Describes the credential required to access the storage, if any.
     * @return the psw
     */
    String getStoragePassword();

    /**
     * Sets cache timeout.
     *
     * @param timeout the cache timeout
     */
    void setStorageTimeout(long timeout);

}
