/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.stat;

/**
 * 
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public interface TicketStatsMBean {
    
    int getNumberOfProxyTicketsVended();
    
    int getNumberOfServiceTicketsVended();
    
    int getNumberOfTicketGrantingTicketsVended();
    
    int getNumberOfProxyGrantingTicketsVended();
    
    int getProxyTicketsPerSecond();
    
    int getServiceTicketsPerSecond();
    
    int getTicketGrantingticketsPerSecond();
    
    int getProxyGrantingTicketsPerSecond();

}
