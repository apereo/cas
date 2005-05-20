/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.stat.support;

import org.jasig.cas.stat.TicketStatistics;
import org.jasig.cas.stat.TicketStatisticsManager;

/**
 * Combined implementation of both the TicketStatistics interface and the Ticket
 * StatisticsManager as a convenience to allow for both retrieval and updating
 * of stats from the same implementation.
 * <p>
 * Statistics are exposed via JMX using Common Attributes.
 * 
 * @@org.springframework.jmx.export.metadata.ManagedResource(description="CAS Ticket
 * Statistics",persistPeriod=1,objectName="cas:id=stats")
 * @author Scott Battaglia
 * @author Dmitriy Kopylenko
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class TicketStatisticsImpl implements TicketStatistics,
    TicketStatisticsManager {

    /** The number of proxy tickets vended. */
    private int numberOfProxyTicketsVended;

    /** The number of service tickets vended. */
    private int numberOfServiceTicketsVended;

    /** The number of TicketGrantingTickets vended. */
    private int numberOfTicketGrantingTicketsVended;

    /** The number of ProxyGrantingTickets vended. */
    private int numberOfProxyGrantingTicketsVended;

    /** The time the TicketStatisticsManager was started. */
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
     * @@org.springframework.jmx.export.metadata.ManagedAttribute(description="The number
     * of service tickets vended since the last reboot.",persistPeriod=1)
     */
    public int getNumberOfServiceTicketsVended() {
        return this.numberOfServiceTicketsVended;
    }

    /**
     * @@org.springframework.jmx.export.metadata.ManagedAttribute(description="The number
     * of ticket granting tickets vended since the last
     * reboot.",persistPeriod=1)
     */
    public int getNumberOfTicketGrantingTicketsVended() {
        return this.numberOfTicketGrantingTicketsVended;
    }

    /**
     * @@org.springframework.jmx.export.metadata.ManagedAttribute(description="The number
     * of proxy granting tickets vended since the last reboot.",persistPeriod=1)
     */
    public int getNumberOfProxyGrantingTicketsVended() {
        return this.numberOfProxyGrantingTicketsVended;
    }

    /**
     * @@org.springframework.jmx.export.metadata.ManagedAttribute(description="The average
     * number of proxy tickets vended per second.",persistPeriod=1)
     */
    public double getProxyTicketsPerSecond() {
        return getTicketsPerSecond(this.numberOfProxyTicketsVended);
    }

    /**
     * @@org.springframework.jmx.export.metadata.ManagedAttribute(description="The average
     * number of service tickets vended per second.",persistPeriod=1)
     */
    public double getServiceTicketsPerSecond() {
        return getTicketsPerSecond(this.numberOfServiceTicketsVended);
    }

    /**
     * @@org.springframework.jmx.export.metadata.ManagedAttribute(description="The average
     * number of ticket granting tickets vended per second.",persistPeriod=1)
     */
    public double getTicketGrantingticketsPerSecond() {
        return getTicketsPerSecond(this.numberOfTicketGrantingTicketsVended);
    }

    /**
     * @@org.springframework.jmx.export.metadata.ManagedAttribute(description="The average
     * number of proxy granting tickets vended per second.",persistPeriod=1)
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

    private double getTicketsPerSecond(final int numberOfTickets) {
        final int numberOfMillisecondsInASecond = 1000;
        final long elapsedTime = (System.currentTimeMillis() - this.startUpTime)
            / numberOfMillisecondsInASecond;

        return (elapsedTime == 0) ? 0.0 : (numberOfTickets / elapsedTime);
    }
}
