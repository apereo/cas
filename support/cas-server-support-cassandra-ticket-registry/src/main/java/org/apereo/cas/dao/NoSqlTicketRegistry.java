package org.apereo.cas.dao;

import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
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
import java.util.List;

@Component("ticketRegistry")
public class NoSqlTicketRegistry extends AbstractTicketRegistry implements TicketRegistryCleaner {

	private static final Logger LOG = LoggerFactory.getLogger(NoSqlTicketRegistry.class);
    private static final String TICKET_GRANTING_TICKET_PREFIX = "TGT";
    private static final String SERVICE_TICKET_PREFIX = "ST";

    private NoSqlTicketRegistryDao ticketRegistryDao;
	private ExpirationCalculator expirationCalculator;
    private LogoutManager logoutManager;
    private boolean logUserOutOfServices = true;

    @Autowired
    public NoSqlTicketRegistry(@Qualifier("cassandraDao") final NoSqlTicketRegistryDao ticketRegistryDao, final  ExpirationCalculator expirationCalculator, final LogoutManager logoutManager, @Value("true") final boolean logUserOutOfServices) {
        this.ticketRegistryDao = ticketRegistryDao;
        this.expirationCalculator = expirationCalculator;
        this.logoutManager = logoutManager;
        this.logUserOutOfServices = logUserOutOfServices;
    }

	@Override
	public void addTicket(final Ticket ticket) {
        LOG.debug("INSERTING TICKET {}", ticket.getId());
        if (ticket instanceof TicketGrantingTicketImpl) {
            TicketGrantingTicketImpl tgt = (TicketGrantingTicketImpl) ticket;
            ticketRegistryDao.addTicketGrantingTicket(ticket);
	        ticketRegistryDao.addTicketToExpiryBucket(ticket, expirationCalculator.getExpiration(tgt));
	    } else if (ticket instanceof ServiceTicket) {
            ticketRegistryDao.addServiceTicket(ticket);
	    } else {
	        LOG.error("inserting unknown ticket type {}", ticket.getClass().getName());
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
	public boolean deleteSingleTicket(String id) {
		LOG.debug("DELETING TICKET {}", id);
		if (isTgt(id)) {
			return ticketRegistryDao.deleteTicketGrantingTicket(id);
		} else if (isSt(id)) {
			return ticketRegistryDao.deleteServiceTicket(id);
		} else {
			LOG.error("deleting unknown ticket type {}", id);
			return false;
		}
	}

	@Override
	public Ticket getTicket(final String id) {
        LOG.debug("QUERYING TICKET {}", id);
	    if (isTgt(id)) {
	        return ticketRegistryDao.getTicketGrantingTicket(id);
	    } else if (isSt(id)) {
            return ticketRegistryDao.getServiceTicket(id);
	    } else {
            LOG.error("requesting unknown ticket type {}", id);
            return null;
	    }
	}

    @Override
    public void updateTicket(final Ticket ticket) {
		LOG.debug("UPDATING TICKET {}", ticket.getId());
        if (ticket instanceof TicketGrantingTicketImpl) {
            TicketGrantingTicketImpl tgt = (TicketGrantingTicketImpl) ticket;
            ticketRegistryDao.updateTicketGrantingTicket(ticket);
            ticketRegistryDao.addTicketToExpiryBucket(ticket, expirationCalculator.getExpiration(tgt));
        } else if (ticket instanceof ServiceTicket) {
            ticketRegistryDao.updateServiceTicket(ticket);
        } else {
			LOG.error("inserting unknown ticket type {}", ticket.getClass().getName());
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
        List<TicketGrantingTicket> expiredTgts = ticketRegistryDao.getExpiredTgts();

        int deleted = 0;
        for (TicketGrantingTicket ticket : expiredTgts) {
            if (logUserOutOfServices) {
                logoutManager.performLogout(ticket);
            }
            deleteTicket(ticket.getId());
            deleted++;
        }

        LOG.info("Finished cleaning Ticket Granting Tickets, {} removed.", deleted);
    }

    private boolean isSt(final String id) {
        return id.startsWith(SERVICE_TICKET_PREFIX);
    }

    private boolean isTgt(final String id) {
        return id.startsWith(TICKET_GRANTING_TICKET_PREFIX);
    }
}
