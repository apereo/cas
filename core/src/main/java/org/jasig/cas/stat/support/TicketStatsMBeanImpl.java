/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.stat.support;

import org.jasig.cas.stat.TicketStatsMBean;


public class TicketStatsMBeanImpl implements TicketStatsMBean {
    private int numberOfProxyTicketsVended;
    private int numberOfServiceTicketesVended;
    private int numberOfTicketGrantingTicketsVended;
    private int numberOfProxyGrantingTicketsVended;
    
    private long startUpTime;
    
    public TicketStatsMBeanImpl() {
        this.startUpTime = System.currentTimeMillis();
        this.numberOfProxyGrantingTicketsVended = 0;
        this.numberOfProxyTicketsVended = 0;
        this.numberOfServiceTicketesVended = 0;
        this.numberOfTicketGrantingTicketsVended = 0;
    }
    
    public int getNumberOfProxyTicketsVended() {
        return this.numberOfProxyTicketsVended;
    }

    public int getNumberOfServiceTicketsVended() {
        return this.numberOfServiceTicketesVended;
    }

    public int getNumberOfTicketGrantingTicketsVended() {
        return this.numberOfTicketGrantingTicketsVended;
    }

    public int getNumberOfProxyGrantingTicketsVended() {
        return this.numberOfProxyGrantingTicketsVended;
    }

    public int getProxyTicketsPerSecond() {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getServiceTicketsPerSecond() {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getTicketGrantingticketsPerSecond() {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getProxyGrantingTicketsPerSecond() {
        // TODO Auto-generated method stub
        return 0;
    }
    
    public void incrementNumberOfProxyGrantingTicketsVended() {
        this.numberOfProxyGrantingTicketsVended++;
    }
    
    public void incrementNumberOfProxyTicketsVended() {
        this.numberOfProxyTicketsVended++;
    }
    
    public void incrementNumberOfServiceTicketsVended() {
        this.numberOfServiceTicketesVended++;
    }
    
    public void incrementNumberOfTicketGrantingTicketsVended() {
        this.numberOfTicketGrantingTicketsVended++;
    }
}
