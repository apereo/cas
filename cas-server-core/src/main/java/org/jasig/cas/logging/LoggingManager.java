/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.logging;

import java.util.List;

/**
 * Interface representing the logging capabilities of CAS.  Very simple
 * logging takes a LogRequest and logs it somewhere.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1.2
 *
 */
public interface LoggingManager {
    
    /**
     * Logs the request to somewhere (implementation dependent).
     * 
     * @param request the request to log.
     */
    void log(LogRequest request);
    
    /**
     * Allows one to search for specific LogRequests based on some criteria.
     * 
     * @param logSearchRequest the criteria to search on.
     * @return the LogRequests matching the search criteria.
     */
    List<LogRequest> search(LogSearchRequest logSearchRequest);
}
