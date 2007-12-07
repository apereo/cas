/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.logging.statistics;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Keeps track of basic statistics for events that occur within the CAS system.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1.2
 *
 */
@Entity
public class Statistic {
    
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private int id;
    
    @Embedded
    private TimePeriod timePeriod;
    
    private int weekDay;
    
    private int hour;
    
    private int minute;
    
    private int numberOfPoints = 0;
    
    private String eventType;
    
    private long minimum = Long.MAX_VALUE;
    
    private long maximum = Long.MIN_VALUE;
    
    private BigDecimal average = new BigDecimal("0.0");
    
    private Date lastUpdate = null;
    
    public Statistic() {
        // nothing to do
    }
    
    public Statistic(final TimePeriod timePeriod, final int weekDay, final int hour, final int minute, final String eventType) {
        this.timePeriod = timePeriod;
        this.weekDay = weekDay;
        this.hour = hour;
        this.minute = minute;
        this.eventType = eventType;
    }
    
    public final synchronized void update(final long newPoint, final Date newLastUpdate) {
        if (newPoint < this.minimum) {
            this.minimum = newPoint;
        }
        
        if (newPoint > this.maximum) {
            this.maximum = newPoint;
        }
        
        this.average = this.average.multiply(new BigDecimal(this.numberOfPoints)).add(new BigDecimal(newPoint)).divide(new BigDecimal(this.numberOfPoints+1));
        this.numberOfPoints++;
        this.lastUpdate = newLastUpdate;
    }
    
    public final int getId() {
        return this.id;
    }

    
    public final void setId(final int id) {
        this.id = id;
    }

    public final TimePeriod getTimePeriod() {
        return this.timePeriod;
    }
    
    public final void setTimePeriod(final TimePeriod timePeriod) {
        this.timePeriod = timePeriod;
    }

    public final int getWeekDay() {
        return this.weekDay;
    }
    
    public final void setWeekDay(final int weekDay) {
        this.weekDay = weekDay;
    }
    
    public final int getHour() {
        return this.hour;
    }
    
    public final void setHour(final int hour) {
        this.hour = hour;
    }
    
    public final int getMinute() {
        return this.minute;
    }
    
    public final void setMinute(final int minute) {
        this.minute = minute;
    }
    
    public final int getNumberOfPoints() {
        return this.numberOfPoints;
    }
    
    public final void setNumberOfPoints(final int numberOfPoints) {
        this.numberOfPoints = numberOfPoints;
    }
    
    public final String getEventType() {
        return this.eventType;
    }
    
    public final void setEventType(final String eventType) {
        this.eventType = eventType;
    }
    
    public final long getMinimum() {
        return this.minimum;
    }
    
    public final void setMinimum(final long minimum) {
        this.minimum = minimum;
    }
    
    public final long getMaximum() {
        return this.maximum;
    }
    
    public final void setMaximum(final long maximum) {
        this.maximum = maximum;
    }
    
    public final BigDecimal getAverage() {
        return this.average;
    }
    
    public final void setAverage(final BigDecimal average) {
        this.average = average;
    }

    public final Date getLastUpdate() {
        return this.lastUpdate;
    }

    public final void setLastUpdate(final Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
