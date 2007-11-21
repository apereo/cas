/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.logging;

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
        return getJpaTemplate().find("Select l from LogRequest l WHERE l.eventType = ?1 AND l.clientInfo.requestDate >= ?2", eventType, fromDate);
    }

    public List<LogRequest> findByPrincipal(final String principal, final Date fromDate) {
        return getJpaTemplate().find("Select l from LogRequest l WHERE LOWER(l.principal) = ?1 AND l.clientInfo.requestDate >= ?2", principal.toLowerCase(), fromDate);
    }

    public List<LogRequest> retrieveByDateFrom(final Date fromDate) {
        return getJpaTemplate().find("Select l from LogRequest l WHERE l.clientInfo.requestDate >= ?1", fromDate);
    }
}
