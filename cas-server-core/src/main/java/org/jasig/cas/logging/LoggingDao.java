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
    
    List<LogRequest> findByPrincipal(String principal, Date fromDate);
    
    List<LogRequest> findByEventType(String eventType, Date fromDate);
    
    List<LogRequest> retrieveByDateFrom(Date fromDate);
}
