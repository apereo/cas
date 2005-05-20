/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.stat;

/**
 * TicketStatistics SPI for CAS core. Interface to allow a manager to update the
 * store maintaining the count of tickets vended.
 * 
 * @author Dmitriy Kopylenko
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public interface TicketStatisticsManager {

    /**
     * Method to increment the number of proxy granting tickets vended.
     */
    void incrementNumberOfProxyGrantingTicketsVended();

    /**
     * Method to increment the number of proxy tickets vended.
     */
    void incrementNumberOfProxyTicketsVended();

    /**
     * Method to increment the number of service tickets vended.
     */
    void incrementNumberOfServiceTicketsVended();

    /**
     * Method to increment the number of ticket granting tickets vended.
     */
    void incrementNumberOfTicketGrantingTicketsVended();
}
