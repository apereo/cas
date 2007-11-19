/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.logging;

import org.jasig.cas.util.annotation.NotNull;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1.2
 *
 */
public final class MultiThreadedLoggingManagerImpl implements LoggingManager {
    
    @NotNull
    private final LoggingDao loggingDao;
    
    @NotNull
    private ThreadPoolTaskExecutor executor;
    
    public MultiThreadedLoggingManagerImpl(final LoggingDao loggingDao) {
        this.loggingDao = loggingDao;
        this.executor = new ThreadPoolTaskExecutor();
    }

    public void log(final LogRequest request) {
        // TODO Auto-generated method stub

    }
}
