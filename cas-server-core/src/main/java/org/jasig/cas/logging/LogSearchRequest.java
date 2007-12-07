/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.logging;

import java.util.Calendar;
import java.util.Date;

/**
 * Object to represent a search amongst the available log entries.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1.2
 *
 */
public class LogSearchRequest {
    
    /** The user to search for. */
    private String principal;
    
    /** The type of event to search for. */
    private String eventType;
    
    /** What date to start searching from. */
    private Date dateFrom;
    
    public LogSearchRequest() {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY)-1);
        this.dateFrom = calendar.getTime();
    }
    
    public String getPrincipal() {
        return this.principal;
    }

    public String getEventType() {
        return this.eventType;
    }

    public Date getDateFrom() {
        return this.dateFrom;
    }
    
    public void setPrincipal(final String principal) {
        this.principal = principal;
    }

    public void setEventType(final String eventType) {
        this.eventType = eventType;
    }

    public void setDateFrom(final Date dateFrom) {
        this.dateFrom = dateFrom;
    }
}
