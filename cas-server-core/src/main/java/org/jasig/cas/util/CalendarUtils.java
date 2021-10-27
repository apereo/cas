/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
