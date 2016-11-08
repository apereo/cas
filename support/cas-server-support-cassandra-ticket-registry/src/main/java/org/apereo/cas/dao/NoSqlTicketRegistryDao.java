package org.apereo.cas.dao;

import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;

import java.util.stream.Stream;

public interface NoSqlTicketRegistryDao {

    void addTicketGrantingTicket(Ticket ticket);
    void addServiceTicket(Ticket ticket);

    boolean deleteTicketGrantingTicket(String id);
    boolean deleteServiceTicket(String id);

    TicketGrantingTicket getTicketGrantingTicket(String id);
    Ticket getServiceTicket(String id);

    void updateTicketGrantingTicket(Ticket ticket);
    void updateServiceTicket(Ticket ticket);

    void addTicketToExpiryBucket(Ticket ticket, long expirationTime);
 
    void updateLastRunTimestamp(long timestamp);
    long getLastRunTimestamp();
    Stream<TicketGrantingTicket> getExpiredTgts();
    void removeRowFromTicketCleanerBucket(long lastRun);
}
