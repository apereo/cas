package org.jasig.cas.ticket.registry.support;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.registry.BatchableTicketRegistry;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.ArrayList;
import java.util.List;

public class BatchedTicketRegistryCleanerTests
{
    private BatchedTicketRegistryCleaner registryCleaner;

    private BatchableTicketRegistry ticketRegistry;
    private BatchedTicketRegistryCleaner.BatchedTicketRetriever ticketRetriever;
    private TicketRegistryCleanerHelper helper;

    @Before
    public void setup()
    {
        ticketRegistry = mock(BatchableTicketRegistry.class);
        ticketRetriever = mock(BatchedTicketRegistryCleaner.BatchedTicketRetriever.class);
        helper = mock(TicketRegistryCleanerHelper.class);

        registryCleaner = new BatchedTicketRegistryCleaner();
        registryCleaner.setTicketRegistry(ticketRegistry);
        registryCleaner.setRegistryCleanerHelper(helper);
    }

    @Test
    public void cleanTickets_whenBatchIsNull_zeroTicketsAreRemoved()
    {
        when(ticketRetriever.getBatch(anyInt(), anyInt())).thenReturn(null);

        int numTicketsRemoved = registryCleaner.cleanTickets(ticketRetriever);

        verifyZeroInteractions(helper);
        assertEquals(0, numTicketsRemoved);
    }

    @Test
    public void cleanTickets_whenBatchIsEmpty_zeroTicketsAreRemoved()
    {
        when(ticketRetriever.getBatch(anyInt(), anyInt())).thenReturn(new ArrayList<Ticket>());

        int numTicketsRemoved = registryCleaner.cleanTickets(ticketRetriever);

        verifyZeroInteractions(helper);
        assertEquals(0, numTicketsRemoved);
    }

    @Test
    public void cleanTickets_whenMultipleBatchesWithNoExpiredTickets_offsetIsUpdatedCorrectly()
    {
        final int batchSize = 100;

        List<Ticket> batch1 = getListOfNTickets(batchSize);
        List<Ticket> batch2 = getListOfNTickets(batchSize);
        List<Ticket> batch3 = getListOfNTickets(75);

        when(ticketRetriever.getBatch(anyInt(), anyInt()))
                .thenReturn(batch1)
                .thenReturn(batch2)
                .thenReturn(batch3)
                .thenReturn(null);

        when(helper.deleteExpiredTickets(any(TicketRegistry.class), anyCollection(), anyBoolean()))
                .thenReturn(0)
                .thenReturn(0)
                .thenReturn(0);

        registryCleaner.setBatchSize(batchSize);
        registryCleaner.cleanTickets(ticketRetriever);

        InOrder inOrder = inOrder(ticketRetriever);
        inOrder.verify(ticketRetriever).getBatch(0, batchSize);
        inOrder.verify(ticketRetriever).getBatch(100, batchSize);
        inOrder.verify(ticketRetriever).getBatch(200, batchSize);
        inOrder.verify(ticketRetriever).getBatch(275, batchSize);
    }

    @Test
    public void cleanTickets_whenMultipleBatchesWithSomeExpiredTickets_offsetIsUpdatedCorrectly()
    {
        final int batchSize = 100;

        List<Ticket> batch1 = getListOfNTickets(batchSize);
        List<Ticket> batch2 = getListOfNTickets(batchSize);
        List<Ticket> batch3 = getListOfNTickets(75);

        final int batch1ExpiredTickets = 25;
        final int batch2ExpiredTickets = 40;
        final int batch3ExpiredTickets = 15;

        when(ticketRetriever.getBatch(anyInt(), anyInt()))
                .thenReturn(batch1)
                .thenReturn(batch2)
                .thenReturn(batch3)
                .thenReturn(null);

        when(helper.deleteExpiredTickets(any(TicketRegistry.class), anyCollection(), anyBoolean()))
                .thenReturn(batch1ExpiredTickets)
                .thenReturn(batch2ExpiredTickets)
                .thenReturn(batch3ExpiredTickets);

        registryCleaner.setBatchSize(batchSize);
        registryCleaner.cleanTickets(ticketRetriever);

        InOrder inOrder = inOrder(ticketRetriever);
        inOrder.verify(ticketRetriever).getBatch(0, batchSize);
        inOrder.verify(ticketRetriever).getBatch(75, batchSize);
        inOrder.verify(ticketRetriever).getBatch(135, batchSize);
        inOrder.verify(ticketRetriever).getBatch(195, batchSize);
    }

    @Test
    public void cleanTickets_whenMultipleBatchesWithAllExpiredTickets_offsetIsUpdatedCorrectly()
    {
        final int batchSize = 100;

        List<Ticket> batch1 = getListOfNTickets(batchSize);
        List<Ticket> batch2 = getListOfNTickets(batchSize);
        List<Ticket> batch3 = getListOfNTickets(75);

        when(ticketRetriever.getBatch(anyInt(), anyInt()))
                .thenReturn(batch1)
                .thenReturn(batch2)
                .thenReturn(batch3)
                .thenReturn(null);

        when(helper.deleteExpiredTickets(any(TicketRegistry.class), anyCollection(), anyBoolean()))
                .thenReturn(batchSize)
                .thenReturn(batchSize)
                .thenReturn(75);

        registryCleaner.setBatchSize(batchSize);
        registryCleaner.cleanTickets(ticketRetriever);

        verify(ticketRetriever, times(4)).getBatch(0, batchSize);
    }

    @Test
    public void clean_processesBothTicketGrantingTicketsAndServiceTickets()
    {
        registryCleaner.clean();

        verify(ticketRegistry).getTicketGrantingTicketBatch(anyInt(), anyInt());
        verify(ticketRegistry).getServiceTicketBatch(anyInt(), anyInt());
    }

    private List<Ticket> getListOfNTickets(int size)
    {
        List<Ticket> tickets = new ArrayList<Ticket>(size);

        for (int i=0; i<size; i++)
        {
            tickets.add(mock(Ticket.class));
        }

        return tickets;
    }
}
