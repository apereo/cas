/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.stat.support;

import org.jasig.cas.stat.TicketStatistics;
import org.jasig.cas.stat.TicketStatisticsManager;

/**
 * 
 * @author Scott Battaglia
 * @author Dmitriy Kopylenko
 * @version $Id$
 *
 */
public class TicketStatisticsImpl implements TicketStatistics, TicketStatisticsManager {
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
    
    public int getNumberOfProxyTicketsVended() {
        return this.numberOfProxyTicketsVended;
    }

    public int getNumberOfServiceTicketsVended() {
        return this.numberOfServiceTicketsVended;
    }

    public int getNumberOfTicketGrantingTicketsVended() {
        return this.numberOfTicketGrantingTicketsVended;
    }

    public int getNumberOfProxyGrantingTicketsVended() {
        return this.numberOfProxyGrantingTicketsVended;
    }

    public double getProxyTicketsPerSecond() {
        return getTicketsPerSecond(this.numberOfProxyTicketsVended);
    }

    public double getServiceTicketsPerSecond() {
        return getTicketsPerSecond(this.numberOfServiceTicketsVended);
    }

    public double getTicketGrantingticketsPerSecond() {
        return getTicketsPerSecond(this.numberOfTicketGrantingTicketsVended);
    }

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
        return (numberOfTickets / elapsedTime);
    }
}
