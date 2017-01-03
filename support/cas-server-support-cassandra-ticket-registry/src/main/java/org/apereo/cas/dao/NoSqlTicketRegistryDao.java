package org.apereo.cas.dao;

import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;

import java.util.stream.Stream;

/**
 * @author David Rodriguez
 *
 * @since 5.1.0
 */
public interface NoSqlTicketRegistryDao {

    /**
     * save a ticketGrantingTicket.
     *
     * @param ticket ticketGrantingTicket to be saved
     */
    void addTicketGrantingTicket(Ticket ticket);

    /**
     * save a serviceTicket.
     *
     * @param ticket serviceTicket to be saved
     */
    void addServiceTicket(Ticket ticket);

    /**
     * Remove a ticketGrantingTicket.
     *
     * @param id ticket's id to be removed
     *
     * @return if ticket was removed
     */
    boolean deleteTicketGrantingTicket(String id);

    /**
     * Remove a serviceTicket.
     *
     * @param id ticket's id to be removed
     *
     * @return if ticket was removed
     */
    boolean deleteServiceTicket(String id);

    /**
     * Retrieve a ticketGrantingTicket.
     * @param id ticket's id to be retrieved
     * @return the ticket found
     */
    TicketGrantingTicket getTicketGrantingTicket(String id);

    /**
     * Retrieve a serviceTicketTicket.
     * @param id ticket's id to be retrieved
     * @return the ticket found
     */
    Ticket getServiceTicket(String id);

    /**
     * update a ticketGrantingTicket.
     *
     * @param ticket ticketGrantingTicket to be updated
     */
    void updateTicketGrantingTicket(Ticket ticket);

    /**
     * update a serviceTicket.
     *
     * @param ticket serviceTicket to be updated
     */
    void updateServiceTicket(Ticket ticket);

    /**
     * Save ticket to the expire collection within the expirationTime bucket.
     * @param ticket ticket to be saved
     * @param expirationTime bucket in which the ticket will be saved
     */
    void addTicketToExpiryBucket(Ticket ticket, long expirationTime);

    /**
     * Remove bucket from expiry collection.
     * @param lastRun bucket to be removed
     */
    void removeRowFromTicketCleanerBucket(long lastRun);

    /**
     * Update the lastRunTimestamp.
     * @param timestamp new timestamp to replace the previous one
     */
    void updateLastRunTimestamp(long timestamp);

    /**
     * Retrieve the lastRunTimestamp.
     * @return lastRunTimestamp
     */
    long getLastRunTimestamp();

    /**
     * Return a Stream as there are more operations to do.
     *
     * @return {@link Stream}
     */
    Stream<TicketGrantingTicket> getExpiredTgts();
}
