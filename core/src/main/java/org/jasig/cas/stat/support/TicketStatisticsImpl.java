/*
 * Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.stat.support;

import org.jasig.cas.stat.TicketStatistics;
import org.jasig.cas.stat.TicketStatisticsManager;

/**
 * @@org.springframework.jmx.export.metadata.ManagedResource(description="CAS Ticket Statistics",persistPeriod=1,objectName="cas:id=stats")
 * @author Scott Battaglia
 * @author Dmitriy Kopylenko
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class TicketStatisticsImpl implements TicketStatistics,
    TicketStatisticsManager {

    private int numberOfProxyTicketsVended;

    private int numberOfServiceTicketsVended;

    private int numberOfTicketGrantingTicketsVended;

    private int numberOfProxyGrantingTicketsVended;

    private long startUpTime;

    public TicketStatisticsImpl() {
        this.startUpTime = System.currentTimeMillis();
        this.numberOfProxyGrantingTicketsVended = 0;
        this.numberOfProxyTicketsVended = 0;
        this.numberOfServiceTicketsVended = 0;
        this.numberOfTicketGrantingTicketsVended = 0;
    }

    /**
     * @@org.springframework.jmx.export.metadata.ManagedAttribute(description="The number
     * of proxy tickets vended since the last reboot.",persistPeriod=1)
     */
    public int getNumberOfProxyTicketsVended() {
        return this.numberOfProxyTicketsVended;
    }

    /**
     * @@org.springframework.jmx.export.metadata.ManagedAttribute(description="The number of service tickets vended since the last reboot.",persistPeriod=1)
     */
    public int getNumberOfServiceTicketsVended() {
        return this.numberOfServiceTicketsVended;
    }

    /**
     * @@org.springframework.jmx.export.metadata.ManagedAttribute(description="The number of ticket granting tickets vended since the last
     * reboot.",persistPeriod=1)
     */
    public int getNumberOfTicketGrantingTicketsVended() {
        return this.numberOfTicketGrantingTicketsVended;
    }

    /**
     * @@org.springframework.jmx.export.metadata.ManagedAttribute(description="The number of proxy granting tickets vended since the last reboot.",persistPeriod=1)
     */
    public int getNumberOfProxyGrantingTicketsVended() {
        return this.numberOfProxyGrantingTicketsVended;
    }

    /**
     * @@org.springframework.jmx.export.metadata.ManagedAttribute(description="The average number of proxy tickets vended per second.",persistPeriod=1)
     */
    public double getProxyTicketsPerSecond() {
        return getTicketsPerSecond(this.numberOfProxyTicketsVended);
    }

    /**
     * @@org.springframework.jmx.export.metadata.ManagedAttribute(description="The average number of service tickets vended per second.",persistPeriod=1)
     */
    public double getServiceTicketsPerSecond() {
        return getTicketsPerSecond(this.numberOfServiceTicketsVended);
    }

    /**
     * @@org.springframework.jmx.export.metadata.ManagedAttribute(description="The average number of ticket granting tickets vended per second.",persistPeriod=1)
     */
    public double getTicketGrantingticketsPerSecond() {
        return getTicketsPerSecond(this.numberOfTicketGrantingTicketsVended);
    }

    /**
     * @@org.springframework.jmx.export.metadata.ManagedAttribute(description="The average number of proxy granting tickets vended per second.",persistPeriod=1)
     */
    public double getProxyGrantingTicketsPerSecond() {
        return getTicketsPerSecond(this.numberOfProxyGrantingTicketsVended);
    }

    public void incrementNumberOfProxyGrantingTicketsVended() {
        this.numberOfProxyGrantingTicketsVended++;
    }

    public void incrementNumberOfProxyTicketsVended() {
        this.numberOfProxyTicketsVended++;
    }

    public void incrementNumberOfServiceTicketsVended() {
        this.numberOfServiceTicketsVended++;
    }

    public void incrementNumberOfTicketGrantingTicketsVended() {
        this.numberOfTicketGrantingTicketsVended++;
    }

    private double getTicketsPerSecond(int numberOfTickets) {
        long elapsedTime = (System.currentTimeMillis() - this.startUpTime) / 1000;

        return (elapsedTime == 0) ? 0.0 : (numberOfTickets / elapsedTime);
    }
}
