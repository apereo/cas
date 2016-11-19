package org.apereo.cas.ticket.registry;

import com.google.common.collect.Lists;
import org.apereo.cas.ticket.Ticket;

import javax.annotation.PostConstruct;
import java.util.Collection;


/**
 * This is {@link JwtTicketRegistry}, which manages and issues
 * CAS tickets in form of JWTs. This registry is intended
 * to be used for stateless CAS deployments as there is no underlying
 * cache/storage backing the JWTs.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class JwtTicketRegistry extends AbstractTicketRegistry {


    /**
     * Instantiates a new Infinispan ticket registry.
     */
    public JwtTicketRegistry() {
    }

    /**
     * Init.
     */
    @PostConstruct
    public void init() {
        logger.info("Setting up JWT Ticket Registry...");
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        return ticket;
    }

    @Override
    public void addTicket(final Ticket ticketToAdd) {
        final Ticket ticket = encodeTicket(ticketToAdd);

        final long idleTime = ticket.getExpirationPolicy().getTimeToIdle() <= 0
                ? ticket.getExpirationPolicy().getTimeToLive()
                : ticket.getExpirationPolicy().getTimeToIdle();

        logger.debug("Adding ticket {} to live {} seconds and stay idle for {} seconds",
                ticket.getId(), ticket.getExpirationPolicy().getTimeToLive(), idleTime);



    }

    @Override
    public Ticket getTicket(final String ticketId) {
        final String encTicketId = encodeTicketId(ticketId);
        if (ticketId == null) {
            return null;
        }
        final Ticket ticket = null;

        return ticket;
    }

    @Override
    public boolean deleteSingleTicket(final String ticketId) {
        logger.warn("deleteSingleTicket() is not supported by {}", getClass().getSimpleName());
        return true;
    }


    @Override
    public Collection<Ticket> getTickets() {
        logger.debug("getTickets() is not supported by {}", getClass().getSimpleName());
        return Lists.newArrayList();
    }

}
