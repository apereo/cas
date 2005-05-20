/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.stat;

/**
 * TicketStatistics is the API to obtain various stats about <i>Tickets</i> in
 * the CAS server. It exposes the number of tickets vended by type and the
 * number of tickets vended per second by type.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public interface TicketStatistics {

    /**
     * Method to retrieve the number of proxy tickets vended.
     * 
     * @return the number of Proxy Tickets vended
     */
    int getNumberOfProxyTicketsVended();

    /**
     * Method to retrieve the number of service tickets vended.
     * 
     * @return the number of Service Tickets vended
     */
    int getNumberOfServiceTicketsVended();

    /**
     * Method to retrieve the number of ticket granting tickets vended.
     * 
     * @return the number of Ticket Granting Tickets vended
     */
    int getNumberOfTicketGrantingTicketsVended();

    /**
     * Method to retrieve the number of proxy granting tickets vended.
     * 
     * @return the number of Proxy Granting Tickets vended
     */
    int getNumberOfProxyGrantingTicketsVended();

    /**
     * Method to retrieve the number of Proxy Tickets vended per second.
     * 
     * @return the number of Proxy Tickets vended per second.
     */
    double getProxyTicketsPerSecond();

    /**
     * Method to retrieve the number of Service Tickets vended per second.
     * 
     * @return the number of Service Tickets vended per second.
     */
    double getServiceTicketsPerSecond();

    /**
     * Method to retrieve the number of Ticket Granting Tickets vended per
     * second.
     * 
     * @return the number of Ticket Granting Tickets vended per second.
     */
    double getTicketGrantingticketsPerSecond();

    /**
     * Method to retrieve the number of Proxy Granting Tickets vended per
     * second.
     * 
     * @return the number of Proxy Granting Tickets vended per second.
     */
    double getProxyGrantingTicketsPerSecond();
}
