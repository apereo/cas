/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.logging;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jasig.cas.util.annotation.NotNull;

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
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    
    public MultiThreadedLoggingManagerImpl(final LoggingDao loggingDao) {
        this.loggingDao = loggingDao;
    }
    
    public void log(final LogRequest logRequest) {
        this.executorService.execute(new LoggingTask(logRequest, this.loggingDao));
    }
    
    protected class LoggingTask implements Runnable {
        
        private final LoggingDao loggingDao;
        
        private final LogRequest logRequest;
        
        public LoggingTask(final LogRequest logRequest, final LoggingDao loggingDao) {
            this.loggingDao = loggingDao;
            this.logRequest = logRequest;
        }

        public void run() {
            this.loggingDao.save(this.logRequest);
        }
    }
}
