/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.stat;

/**
 * TicketStatistics SPI for CAS core.
 * 
 * @author Dmitriy Kopylenko
 * @author Scott Battaglia
 * @version $Id: TicketStatisticsManager.java,v 1.2 2005/02/27 05:49:26
 * sbattaglia Exp $
 */
public interface TicketStatisticsManager {

    public void incrementNumberOfProxyGrantingTicketsVended();

    public void incrementNumberOfProxyTicketsVended();

    public void incrementNumberOfServiceTicketsVended();

    public void incrementNumberOfTicketGrantingTicketsVended();
}
