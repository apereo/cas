/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.logging;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jasig.cas.util.annotation.NotNull;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

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
    
    @NotNull
    private final TransactionTemplate transactionTemplate;
    
    public MultiThreadedLoggingManagerImpl(final LoggingDao loggingDao, final TransactionTemplate transactionTemplate) {
        this.loggingDao = loggingDao;
        this.transactionTemplate = transactionTemplate;
    }
    
    @Transactional(readOnly=false)
    public void log(final LogRequest logRequest) {
        this.executorService.execute(new LoggingTask(logRequest, this.loggingDao, this.transactionTemplate));
    }

    @Transactional(readOnly=true)
    public List<LogRequest> search(final LogSearchRequest logSearchRequest) {
        final boolean hasPrincipal = StringUtils.hasText(logSearchRequest.getPrincipal());
        final boolean hasEventType = StringUtils.hasText(logSearchRequest.getEventType());
        if (hasPrincipal && hasEventType) {
            return this.loggingDao.findByPrincipalAndEventType(logSearchRequest.getPrincipal(), logSearchRequest.getEventType(), logSearchRequest.getDateFrom());
        }
        
        if (hasPrincipal) {
            return this.loggingDao.findByPrincipal(logSearchRequest.getPrincipal(), logSearchRequest.getDateFrom());
        }
        
        if (hasEventType) {
            return this.loggingDao.findByEventType(logSearchRequest.getEventType(), logSearchRequest.getDateFrom());
        }
        
        return this.loggingDao.retrieveByDateFrom(logSearchRequest.getDateFrom());
    }

    protected class LoggingTask implements Runnable {
        
        final LoggingDao loggingDao;
        
        final LogRequest logRequest;
        
        private final TransactionTemplate transactionTemplate;
        
        public LoggingTask(final LogRequest logRequest, final LoggingDao loggingDao, final TransactionTemplate transactionTemplate) {
            this.loggingDao = loggingDao;
            this.logRequest = logRequest;
            this.transactionTemplate = transactionTemplate;
        }

        public void run() {
              
            this.transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                protected void doInTransactionWithoutResult(
                    final TransactionStatus transactionStatus) {
                    LoggingTask.this.loggingDao.save(LoggingTask.this.logRequest);
                }
            });          
        }
    }
}
