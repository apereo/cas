/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.util;

import java.util.Calendar;
import java.util.Date;


public final class CalendarUtils {
    
    public static final String[] WEEKDAYS = new String[] {"UNDEFINED", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    
    private CalendarUtils() {
        // nothing to do
    }
    
    public static int getCurrentDayOfWeek() {
        return getCurrentDayOfWeekFor(new Date());
    }
    
    public static int getCurrentDayOfWeekFor(final Date date) {
        return  getCalendarFor(date).get(Calendar.DAY_OF_WEEK);
    }
    
    public static Calendar getCalendarFor(final Date date) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }
}
