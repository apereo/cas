/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.stat;

import java.io.Serializable;

/**
 * 
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public interface TicketStatsMBean extends Serializable {
    
    int getNumberOfProxyTicketsVended();
    
    int getNumberOfServiceTicketsVended();
    
    int getNumberOfTicketGrantingTicketsVended();
    
    int getNumberOfProxyGrantingTicketsVended();
    
    double getProxyTicketsPerSecond();
    
    double getServiceTicketsPerSecond();
    
    double getTicketGrantingticketsPerSecond();
    
    double getProxyGrantingTicketsPerSecond();
    
    void incrementNumberOfProxyTicketsVended();
    
    void incrementNumberOfServiceTicketsVended();
    
    void incrementNumberOfTicketGrantingTicketsVended();
    
    void incrementNumberOfProxyGrantingTicketsVended();
}
