package org.apereo.cas.dao;

import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.utils.TicketCreator;
import org.junit.Test;

import java.util.stream.Stream;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class NoSqlTicketRegistryTest {

    @Test
    public void shouldAddATicket() throws Exception {
        NoSqlTicketRegistryDao dao = mock(NoSqlTicketRegistryDao.class);
        NoSqlTicketRegistry ticketRegistry = new NoSqlTicketRegistry(dao, mock(LogoutManager.class), true);

        //when
        ticketRegistry.addTicket(TicketCreator.defaultTGT("id"));

        //then
        verify(dao).addTicketGrantingTicket(any());
    }

    @Test
    public void shouldRetrieveATicket() throws Exception {
        NoSqlTicketRegistryDao dao = mock(NoSqlTicketRegistryDao.class);
        NoSqlTicketRegistry ticketRegistry = new NoSqlTicketRegistry(dao, mock(LogoutManager.class), true);
        String ticketId = "TGT-1234";

        //when
        ticketRegistry.getTicket(ticketId);

        //then
        verify(dao).getTicketGrantingTicket(ticketId);
    }

    @Test
    public void shouldLogUserOutOfServicesWhenPropertySpecified() throws Exception {
        boolean logUserOutOfServices = true;
        LogoutManager logoutManager = mock(LogoutManager.class);
        NoSqlTicketRegistryDao dao = mock(NoSqlTicketRegistryDao.class);
        NoSqlTicketRegistry ticketRegistry = new NoSqlTicketRegistry(dao, logoutManager, logUserOutOfServices);

        when(dao.getExpiredTgts()).thenReturn(Stream.of(TicketCreator.expiredTGT("expiredId")));

        //when
        ticketRegistry.clean();

        //then
        verify(logoutManager).performLogout(any());
    }

    @Test
    public void shouldLogUserOutOfServicesWhenPropertyNotSpecified() throws Exception {
        boolean logUserOutOfServices = false;
        LogoutManager logoutManager = mock(LogoutManager.class);
        NoSqlTicketRegistryDao dao = mock(NoSqlTicketRegistryDao.class);
        NoSqlTicketRegistry ticketRegistry = new NoSqlTicketRegistry(dao, logoutManager, logUserOutOfServices);

        when(dao.getExpiredTgts()).thenReturn(Stream.of(TicketCreator.expiredTGT("expiredId")));

        //when
        ticketRegistry.clean();

        //then
        verify(logoutManager, never()).performLogout(any());
    }
}