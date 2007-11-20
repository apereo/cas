/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.logging;

import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;

/**
 * Implementation of the Dao that saves the log request to the database.
 * <p>
 * The table should follow the following format:
 * <pre>
 * CREATE TABLE CAS_LOGGING
 * (
 *   LOG_DATE           DATE                       NOT NULL,
 *   CLIENT_IP_ADDRESS  VARCHAR2(15 BYTE)          NOT NULL,
 *   CLIENT_USER_AGENT  VARCHAR2(255 BYTE),
 *   PRINCIPAL          VARCHAR2(255 BYTE),
 *   SERVICE            VARCHAR2(255 BYTE),
 *   EVENT_TYPE         VARCHAR2(255 BYTE)         NOT NULL,
 *   SERVER_IP_ADDRESS  VARCHAR2(15 BYTE)          NOT NULL
 * )
 * 
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1.2
 *
 */
public class JdbcLoggingDaoImpl extends SimpleJdbcDaoSupport implements LoggingDao {
    
    private static final String SQL_INSERT_LOG = "Insert into cas_logging(LOG_DATE, CLIENT_IP_ADDRESS, CLIENT_USER_AGENT, PRINCIPAL, SERVICE, EVENT_TYPE, SERVER_IP_ADDRESS) VALUES(?, ?, ?, ?, ?, ?, ?)";

    public void save(final LogRequest logRequest) {
        final String userAgent = logRequest.getClientInfo().getUserAgent();
        final String truncatedUserAgent = userAgent != null && userAgent.length() > 255 ? userAgent.substring(0, 255) : userAgent;
        
        final String service = logRequest.getService();
        final String truncatedService = service != null && service.length() > 255 ? service.substring(0, 255) : service;

        getSimpleJdbcTemplate().update(SQL_INSERT_LOG, logRequest.getClientInfo().getRequestDate(), logRequest.getClientInfo().getClientIpAddress(), truncatedUserAgent, logRequest.getPrincipal(), truncatedService, logRequest.getEventType(), logRequest.getClientInfo().getServerIpAddress());
    }
}
