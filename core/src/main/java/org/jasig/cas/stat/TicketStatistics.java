/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.stat;

/**
 * TicketStatistics is the API to obtain various stats about <i>Tickets</i> in the CAS server.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public interface TicketStatistics {

    public int getNumberOfProxyTicketsVended();

    public int getNumberOfServiceTicketsVended();

    public int getNumberOfTicketGrantingTicketsVended();

    public int getNumberOfProxyGrantingTicketsVended();

    public double getProxyTicketsPerSecond();

    public double getServiceTicketsPerSecond();

    public double getTicketGrantingticketsPerSecond();

    public double getProxyGrantingTicketsPerSecond();

}
