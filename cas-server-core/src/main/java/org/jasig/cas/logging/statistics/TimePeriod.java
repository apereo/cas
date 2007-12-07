/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.logging.statistics;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.Embeddable;

/**
 * For the purposes of the statistics gathering, this allows for more fine-grained
 * analysis of data (for example, Tuesday's during the summer might be radically different
 * then Tuesdays during the Fall semester and their averages should be calculated
 * differently.
 *  
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1.2
 *
 */
@Embeddable
public final class TimePeriod {
    private String name;
    
    private int startMonth;
    
    private int startDay;
    
    private int endMonth;
    
    private int endDay;
    
    public TimePeriod() {
        // nothing to do
    }
    
    public TimePeriod(final long id, final String name, final int startMonth, final int startDay, final int endMonth, final int endDay) {
        this.name = name;
        this.startMonth = startMonth;
        this.startDay = startDay;
        this.endMonth = endMonth;
        this.endDay = endDay;
    }
    
    public String toString() {
        return this.startMonth + "/" + this.startDay + " - " + this.endMonth + "/" + this.endDay;
    }
    
    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    
    public void setStartMonth(final int startMonth) {
        this.startMonth = startMonth;
    }

    
    public void setStartDay(final int startDay) {
        this.startDay = startDay;
    }

    
    public void setEndMonth(final int endMonth) {
        this.endMonth = endMonth;
    }

    
    public void setEndDay(final int endDay) {
        this.endDay = endDay;
    }

    public boolean contains(final Date date) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DATE);
        
        // since its not in any of the outlier months, we don't need to check the day.
        if (month > this.startMonth && month < this.endMonth) {
            return true;
        }
        
        // since the start and end month are the same, we need to check that its within the start and end days.
        if (month == this.startMonth && month == this.endMonth) {
            return day >= this.startDay && day <= this.endDay;
        }
        
        // check the start day
        if (month == this.startMonth) {
            return day >= this.startDay;
        }
        
        if (month == this.endMonth) {
            return day <= this.endDay;
        }
        
        return false;
    }
}
