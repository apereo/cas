/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.stat;

/**
 * TicketStatistics SPI for CAS core.
 * 
 * @author Dmitriy Kopylenko
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public interface TicketStatisticsManager {

    void incrementNumberOfProxyGrantingTicketsVended();

    void incrementNumberOfProxyTicketsVended();

    void incrementNumberOfServiceTicketsVended();

    void incrementNumberOfTicketGrantingTicketsVended();
}
