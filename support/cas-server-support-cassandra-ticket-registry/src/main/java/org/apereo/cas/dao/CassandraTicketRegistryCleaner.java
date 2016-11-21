package org.apereo.cas.dao;

import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;

public class CassandraTicketRegistryCleaner implements TicketRegistryCleaner {

    private final NoSqlTicketRegistryDao ticketRegistryDao;
    private final LogoutManager logoutManager;

    public CassandraTicketRegistryCleaner(@Qualifier("cassandraDao") final NoSqlTicketRegistryDao ticketRegistryDao, final LogoutManager logoutManager) {
        this.ticketRegistryDao = ticketRegistryDao;
        this.logoutManager = logoutManager;
    }

    @Scheduled(initialDelayString = "${cas.ticket.registry.cleaner.startDelay:20000}",
            fixedDelayString = "${cas.ticket.registry.cleaner.repeatInterval:60000}")
    @Override
    public void clean() {
        ticketRegistryDao.getExpiredTgts().forEach(ticket -> {
            logoutManager.performLogout(ticket);
            ticketRegistryDao.deleteTicketGrantingTicket(ticket.getId());
        });
    }
}
