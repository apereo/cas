/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.logging;

import java.util.Date;
import java.util.List;

/**
 * Dao to log requests to a specific location.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1.2
 *
 */
public interface LoggingDao {

    /**
     * Save the log request.
     * 
     * @param logRequest the log request to save.
     */
    void save(LogRequest logRequest);
    
    /**
     * Find the LogRequests matching the principal and from the fromDate forward.
     * 
     * @param principal the principal to match on.
     * @param fromDate the date to start looking from.
     * @return the LogRequests matching the criteria.
     */
    List<LogRequest> findByPrincipal(String principal, Date fromDate);
    
    /**
     * Find the LogRequests matching the event type and from the fromDate forward.
     * 
     * @param eventType the event type to match on.
     * @param fromDate the date to start looking from.
     * @return the LogRequests matching the criteria.
     */
    List<LogRequest> findByEventType(String eventType, Date fromDate);
    
    /**
     * Retrieve all of the log requests from the supplied date forward.
     * 
     * @param fromDate the date to retrieve all of the log requests from.
     * @return the log requests from the supplied date forward.
     */
    List<LogRequest> retrieveByDateRange(Date fromDate, Date toDate);
    
    /**
     * Find the LogRequests matching the event type and from the fromDate forward.
     * 
     * @param principal the principal to match on.
     * @param eventType the event type to match on.
     * @param fromDate the date to start looking from.
     * @return the LogRequests matching the criteria.
     */
    List<LogRequest> findByPrincipalAndEventType(String principal, String eventType, Date fromDate);
    
    /**
     * This method is for the purpose of retrieving log requests from the supplied date to the last complete day.
     * So if today is 1/1/2008 12:55, it will retrieve from the supplied date to 12/31/2007 23:59:59.
     * 
     * @param fromDate the date to start looking from.
     */
    List<LogRequest> retrieveAllLogRequestsSince(Date fromDate);
}
