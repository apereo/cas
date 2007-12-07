/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.logging.statistics;

import java.util.Date;
import java.util.List;

/**
 * Retrieve and persist statistics about the CAS server.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1.2
 *
 */
public interface StatisticsDao {
    
    /** The amount of precision we are looking for in our statistics. */
    enum Precision {MINUTE, HOUR, DAY}
    
    /**
     *  Save the newly calculated statistics.
     *
     *  @param statistic the statistic to save.
     */
    void save(Statistic statistic);
    
    /**
     * Find a particular statistic in order to update it.
     * 
     * @param date the date of the statistic (in order to find the Time Period)
     * @param precision the precision required
     * @param eventType the event type.
     * @return the statistic, if its found.
     */
    Statistic findBy(Date date, Precision precision, String eventType);
    
    /**
     * Find a all the average statistics related to today.
     * 
     * @param date the date of the statistic (in order to find the Time Period)

     * @return the statistic, if its found.
     */
    List<Statistic> findAllBy(Date date);
    
    /**
     * Retrieve all of the statistics with precision daily.
     * 
     * @return  all of the available statistics.
     */
    List<Statistic> getAllDailyStatistics();
    
    /**
     * Retrieve all of the statistics with precision hour.
     * 
     * @return  all of the available statistics.
     */
    List<Statistic> getAllHourlyStatistics();
    
    /**
     * Retrieve all of the statistics with precision minute.
     * 
     * @return  all of the available statistics.
     */
    List<Statistic> getAllMinuteStatistics();
    
    /**
     * Find what the last date/time the statistics were updated.
     * @return the last date/time the statistics were updated, or null if never updated.
     */
    Date getDateLastCalculated();
}
