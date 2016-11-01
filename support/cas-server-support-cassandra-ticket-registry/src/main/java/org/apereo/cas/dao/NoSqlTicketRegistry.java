package org.apereo.cas.dao;

import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.registry.AbstractTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component("ticketRegistry")
public class NoSqlTicketRegistry extends AbstractTicketRegistry implements TicketRegistryCleaner {

    private static final Logger LOGGER = LoggerFactory.getLogger(NoSqlTicketRegistry.class);
    private static final String TICKET_GRANTING_TICKET_PREFIX = "TGT";
    private static final String SERVICE_TICKET_PREFIX = "ST";

    private NoSqlTicketRegistryDao ticketRegistryDao;
    private ExpirationCalculator expirationCalculator;
    private LogoutManager logoutManager;
    private boolean logUserOutOfServices = true;

    @Autowired
    public NoSqlTicketRegistry(@Qualifier("cassandraDao") final NoSqlTicketRegistryDao ticketRegistryDao, final ExpirationCalculator expirationCalculator,
                               final LogoutManager logoutManager, @Value("true") final boolean logUserOutOfServices) {
        this.ticketRegistryDao = ticketRegistryDao;
        this.expirationCalculator = expirationCalculator;
        this.logoutManager = logoutManager;
        this.logUserOutOfServices = logUserOutOfServices;
    }

    @Override
    public void addTicket(final Ticket ticket) {
        final String ticketId = ticket.getId();
        LOGGER.debug("INSERTING TICKET {}", ticketId);
        if (isTgt(ticketId)) {
            final TicketGrantingTicketImpl tgt = (TicketGrantingTicketImpl) ticket;
            ticketRegistryDao.addTicketGrantingTicket(ticket);
            ticketRegistryDao.addTicketToExpiryBucket(ticket, expirationCalculator.getExpiration(tgt));
        } else if (isSt(ticketId)) {
            ticketRegistryDao.addServiceTicket(ticket);
        } else {
            LOGGER.error("inserting unknown ticket type {}", ticket.getClass().getName());
        }
    }

    @Override
    public int deleteTicket(final String id) {
        if (deleteSingleTicket(id)) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public boolean deleteSingleTicket(final String id) {
        LOGGER.debug("DELETING TICKET {}", id);
        if (isTgt(id)) {
            return ticketRegistryDao.deleteTicketGrantingTicket(id);
        } else if (isSt(id)) {
            return ticketRegistryDao.deleteServiceTicket(id);
        } else {
            LOGGER.error("deleting unknown ticket type {}", id);
            return false;
        }
    }

    @Override
    public Ticket getTicket(final String id) {
        LOGGER.debug("QUERYING TICKET {}", id);
        if (isTgt(id)) {
            return ticketRegistryDao.getTicketGrantingTicket(id);
        } else if (isSt(id)) {
            return ticketRegistryDao.getServiceTicket(id);
        } else {
            LOGGER.error("requesting unknown ticket type {}", id);
            return null;
        }
    }

    @Override
    public void updateTicket(final Ticket ticket) {
        final String ticketId = ticket.getId();
        LOGGER.debug("UPDATING TICKET {}", ticketId);
        if (isTgt(ticketId)) {
            final TicketGrantingTicketImpl tgt = (TicketGrantingTicketImpl) ticket;
            ticketRegistryDao.updateTicketGrantingTicket(ticket);
            ticketRegistryDao.addTicketToExpiryBucket(ticket, expirationCalculator.getExpiration(tgt));
        } else if (isSt(ticketId)) {
            ticketRegistryDao.updateServiceTicket(ticket);
        } else {
            LOGGER.error("inserting unknown ticket type {}", ticket.getClass().getName());
        }
    }

    @Override
    public Collection<Ticket> getTickets() {
        return null;
    }

    @Scheduled(initialDelayString = "${cas.ticket.registry.cleaner.startDelay:20000}",
            fixedDelayString = "${cas.ticket.registry.cleaner.repeatInterval:60000}")
    @Override
    public void clean() {
        ticketRegistryDao.getExpiredTgts().forEach(ticket -> {
            if (logUserOutOfServices) {
                logoutManager.performLogout(ticket);
            }
            deleteTicket(ticket.getId());
        });
    }

    private static boolean isSt(final String id) {
        return id.startsWith(SERVICE_TICKET_PREFIX);
    }

    private static boolean isTgt(final String id) {
        return id.startsWith(TICKET_GRANTING_TICKET_PREFIX);
    }
}
