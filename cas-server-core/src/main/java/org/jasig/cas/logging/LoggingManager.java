/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.logging;

import java.util.List;


public interface LoggingManager {
    
    void log(LogRequest request);
    
    List<LogRequest> search(LogSearchRequest logSearchRequest);
}
