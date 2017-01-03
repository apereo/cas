package org.apereo.cas.ticket.registry;

import com.google.common.base.Throwables;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.Ticket;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Collection;


/**
 * A Ticket Registry storage backend based on MongoDB.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class MongoDbTicketRegistry extends AbstractTicketRegistry {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        logger.debug("Updating ticket {}", ticket);
        try {

        } catch (final Exception e) {
            logger.error("Failed updating {}: {}", ticket, e);
        }
        return ticket;
    }

    @Override
    public void addTicket(final Ticket ticketToAdd) {
        logger.debug("Adding ticket {}", ticketToAdd);
        try {

        } catch (final Exception e) {
            logger.error("Failed adding {}: {}", ticketToAdd, e);
        }
    }

    @Override
    public Ticket getTicket(final String ticketId) {
        try {
            logger.debug("Locating ticket id {}", ticketId);
            final String encTicketId = encodeTicketId(ticketId);
            if (encTicketId == null) {
                logger.debug("Ticket id {} could not be found", ticketId);
                return null;
            }


            logger.debug("Ticket {} not found in the registry.", encTicketId);
            return null;
        } catch (final Exception e) {
            logger.error("Failed fetching {}: {}", ticketId, e);
            return null;
        }
    }


    /** Init registry. **/
    @PostConstruct
    public void initialize() {

    }


    /**
     * Stops the client.
     */
    @PreDestroy
    public void destroy() {
        try {

        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public Collection<Ticket> getTickets() {

        return new ArrayList<>();
    }

    @Override
    public long sessionCount() {
        return 0;
    }

    @Override
    public long serviceTicketCount() {
        return 0;
    }

    @Override
    public boolean deleteSingleTicket(final String ticketId) {
        logger.debug("Deleting ticket {}", ticketId);
        try {
            return true;
        } catch (final Exception e) {
            logger.error("Failed deleting {}: {}", ticketId, e);
            return false;
        }
    }
    
    /**
     * Get the expiration policy value of the ticket in seconds.
     *
     * @param ticket the ticket
     * @return the exp value
     * @see <a href="http://docs.couchbase.com/developer/java-2.0/documents-basics.html">Couchbase Docs</a>
     */
    private int getTimeToLive(final Ticket ticket) {
        final int expTime = ticket.getExpirationPolicy().getTimeToLive().intValue();
        
        return expTime;
    }
}

