/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.stat;


/**
 * TicketStatistics SPI for CAS core.
 * 
 * @author Dmitriy Kopylenko
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public interface TicketStatisticsManager {
    
    public void incrementNumberOfProxyGrantingTicketsVended();
    
    public void incrementNumberOfProxyTicketsVended();
    
    public void incrementNumberOfServiceTicketsVended();
    
    public void incrementNumberOfTicketGrantingTicketsVended();
}
