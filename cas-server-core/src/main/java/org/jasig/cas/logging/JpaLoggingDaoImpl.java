/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.logging;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.orm.jpa.support.JpaDaoSupport;

/**
 * Implementation of the Dao that saves the log request to the database using JPA.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1.2
 *
 */
public final class JpaLoggingDaoImpl extends JpaDaoSupport implements LoggingDao {
    
    public void save(final LogRequest logRequest) {
        getJpaTemplate().persist(logRequest);
    }

    public List<LogRequest> findByEventType(final String eventType, final Date fromDate) {
        return getJpaTemplate().find("Select l from LogRequest l WHERE l.eventType = ?1 AND l.clientInfo.requestDate >= ?2 ORDER BY l.clientInfo.requestDate DESC, l.id DESC", eventType, fromDate);
    }
    
    public List<LogRequest> findByPrincipalAndEventType(final String principal, final String eventType, final Date fromDate) {
        return getJpaTemplate().find("Select l from LogRequest l WHERE LOWER(l.principal) LIKE ?1 AND l.eventType = ?2 AND l.clientInfo.requestDate >= ?3 ORDER BY l.clientInfo.requestDate DESC, l.id DESC", principal.toLowerCase() + "%", eventType, fromDate);
    }

    public List<LogRequest> findByPrincipal(final String principal, final Date fromDate) {
        return getJpaTemplate().find("Select l from LogRequest l WHERE LOWER(l.principal) LIKE ?1 AND l.clientInfo.requestDate >= ?2 ORDER BY l.clientInfo.requestDate DESC, l.id DESC", principal.toLowerCase() + "%", fromDate);
    }

    public List<LogRequest> retrieveByDateRange(final Date fromDate, final Date toDate) {
        return getJpaTemplate().find("Select l from LogRequest l WHERE l.clientInfo.requestDate >= ?1 and l.clientInfo.requestDate <= ?2 ORDER BY l.clientInfo.requestDate DESC, l.id DESC", fromDate, toDate);
    }

    /**
     * If no fromDate is specified, it will assume from the last 365 days.
     * @see org.jasig.cas.logging.LoggingDao#retrieveAllLogRequestsSince(java.util.Date)
     */
    public List<LogRequest> retrieveAllLogRequestsSince(final Date fromDate) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        calendar.add(Calendar.DATE, -1);
        
        final Date lastDate;
        
        if (fromDate != null) {
            lastDate = fromDate;
        } else {
            final Calendar lastCalendar = Calendar.getInstance();
            lastCalendar.setTime(new Date());
            lastCalendar.add(Calendar.YEAR, -1);
            lastDate = lastCalendar.getTime();
        }
        
        return getJpaTemplate().find("Select l from LogRequest l WHERE l.clientInfo.requestDate > ?1 and l.clientInfo.requestDate <= ?2 ORDER BY l.clientInfo.requestDate DESC, l.id DESC", lastDate, calendar.getTime());
    }
}
