/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.stat;


/**
 * TicketStatistics is the API to obtain various stats about <i>Tickets</i> in the CAS server.
 * @@org.springframework.jmx.metadata.support.ManagedResource(description="CAS Ticket Statistics", objectName="cas:bean=stats")
 * 
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public interface TicketStatistics {
       
    /**
     * @@org.springframework.jmx.metadata.support.ManagedAttribute(description="The number of proxy tickets vended since the last reboot.")
     */
    public int getNumberOfProxyTicketsVended();

    /**
     * @@org.springframework.jmx.metadata.support.ManagedAttribute(description="The number of service tickets vended since the last reboot.")
     */
    public int getNumberOfServiceTicketsVended();

    /**
     * @@org.springframework.jmx.metadata.support.ManagedAttribute(description="The number of ticket granting tickets vended since the last reboot.")
     */
    public int getNumberOfTicketGrantingTicketsVended();

    /**
     * @@org.springframework.jmx.metadata.support.ManagedAttribute(description="The number of proxy granting tickets vended since the last reboot.")
     */
    public int getNumberOfProxyGrantingTicketsVended();

    /**
     * @@org.springframework.jmx.metadata.support.ManagedAttribute(description="The average number of proxy tickets vended per second.")
     */
    public double getProxyTicketsPerSecond();

    /**
     * @@org.springframework.jmx.metadata.support.ManagedAttribute(description="The average number of service tickets vended per second.")
     */
    public double getServiceTicketsPerSecond();

    /**
     * @@org.springframework.jmx.metadata.support.ManagedAttribute(description="The average number of ticket granting tickets vended per second.")
     */
    public double getTicketGrantingticketsPerSecond();

    /**
     * @@org.springframework.jmx.metadata.support.ManagedAttribute(description="The average number of proxy granting tickets vended per second.")
     */
    public double getProxyGrantingTicketsPerSecond();
    
}
