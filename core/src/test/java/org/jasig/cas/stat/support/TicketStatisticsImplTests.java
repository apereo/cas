/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.stat.support;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class TicketStatisticsImplTests extends TestCase {

    private TicketStatisticsImpl ticketStatistics = new TicketStatisticsImpl();

    public void testProxyGrantingTicketsVended() {
        assertEquals(0, this.ticketStatistics
            .getNumberOfProxyGrantingTicketsVended());
        this.ticketStatistics.incrementNumberOfProxyGrantingTicketsVended();
        assertEquals(1, this.ticketStatistics
            .getNumberOfProxyGrantingTicketsVended());
    }

    public void testProxyTicketsVended() {
        assertEquals(0, this.ticketStatistics.getNumberOfProxyTicketsVended());
        this.ticketStatistics.incrementNumberOfProxyTicketsVended();
        assertEquals(1, this.ticketStatistics.getNumberOfProxyTicketsVended());
    }

    public void testServiceTicketsVended() {
        assertEquals(0, this.ticketStatistics.getNumberOfServiceTicketsVended());
        this.ticketStatistics.incrementNumberOfServiceTicketsVended();
        assertEquals(1, this.ticketStatistics.getNumberOfServiceTicketsVended());
    }

    public void testTicketGrantingTicketsVended() {
        assertEquals(0, this.ticketStatistics
            .getNumberOfTicketGrantingTicketsVended());
        this.ticketStatistics.incrementNumberOfTicketGrantingTicketsVended();
        assertEquals(1, this.ticketStatistics
            .getNumberOfTicketGrantingTicketsVended());
    }

    public void testTicketGrantingTicketsVendedPerSecond() {
        assertTrue(0 == this.ticketStatistics
            .getTicketGrantingticketsPerSecond());
    }

    public void testProxyGrantingTicketsPerSecond() {
        assertTrue(0 == this.ticketStatistics
            .getProxyGrantingTicketsPerSecond());
    }

    public void testProxyTicketsPerSecond() {
        assertTrue(0 == this.ticketStatistics.getProxyTicketsPerSecond());
    }

    public void testServiceTicketsPerSecond() {
        assertTrue(0 == this.ticketStatistics.getServiceTicketsPerSecond());
    }

    public void testServiceTicketsPerSecondWithSleep() throws Exception {
        Thread.sleep(5000);
        assertTrue(0 == this.ticketStatistics.getServiceTicketsPerSecond());
    }
}
