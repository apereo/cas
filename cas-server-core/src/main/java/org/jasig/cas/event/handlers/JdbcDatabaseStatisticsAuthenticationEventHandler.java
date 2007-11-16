/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.event.handlers;

import java.util.Calendar;
import java.util.Date;

import org.jasig.cas.event.AuthenticationEvent;
import org.jasig.cas.event.EventHandler;
import org.springframework.context.ApplicationEvent;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1.2
 */
public final class JdbcDatabaseStatisticsAuthenticationEventHandler extends SimpleJdbcDaoSupport implements
    EventHandler {

    public void handleEvent(final ApplicationEvent event) {
        final AuthenticationEvent authenticationEvent = (AuthenticationEvent) event;
        

    }

    public boolean supports(final ApplicationEvent event) {
        return event instanceof AuthenticationEvent;
    }
    
    protected int resolveTimePeriod(final Date authenticationDate) {
        // TODO obtain from database
        return 0;
    }
    
    protected class JdbcDatabaseStatisticsAuthenticationEventRunnable implements Runnable {
        
        private final AuthenticationEvent authenticationEvent;
        
        public JdbcDatabaseStatisticsAuthenticationEventRunnable(final AuthenticationEvent authenticationEvent) {
            this.authenticationEvent = authenticationEvent;
        }

        public void run() {
            final Date authenticationDate = this.authenticationEvent.getPublishedDate();
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(authenticationDate);
            final int timePeriod = resolveTimePeriod(authenticationDate);
            final int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
            final int hour = calendar.get(Calendar.HOUR_OF_DAY);
            final int minute = calendar.get(Calendar.MINUTE);
            final String type = this.authenticationEvent.isSuccessfulAuthentication() ? "SUCCESSFUL AUTHENTICATION" : "FAILED AUTHENTICATION";
            
            // TODO Auto-generated method stub
            
        }
        
    }
    
    
}
