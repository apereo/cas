package org.apereo.cas.ticket;

/**
 * Interface that enables for pluggable unique ticket Ids strategies.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@FunctionalInterface
public interface UniqueTicketIdGenerator {

    /**
     * Separator character that separates prefix from
     * the rest of the ticket id.
     */
    char SEPARATOR = '-';

    /**
     * Default ticket size 24 bytes raw, 32 bytes once encoded to base64.
     */
    int TICKET_SIZE = 24;

    /**
     * Return a new unique ticket id beginning with the prefix.
     *
     * @param prefix The prefix we want attached to the ticket.
     * @return the unique ticket id
     * @throws Throwable the throwable
     */
    String getNewTicketId(String prefix) throws Throwable;

    /**
     * Simple ticket id generator.
     *
     * @return the unique ticket id generator
     */
    static UniqueTicketIdGenerator prefixedTicketIdGenerator() {
        return prefix -> prefix;
        
    }
}
