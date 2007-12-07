/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.logging.statistics;

import java.util.List;

/**
 * Processes and calculates the latest statistics from the most recent logs.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1.2
 *
 */
public interface StatisticsManager {

    /**
     * Processes the latest log requests and stores the statistics in the database.
     */
    void processLatestLogRequests();
    
    List<Statistic> getDailyStatistics();
    
    List<Statistic> getHourlyStatistics();
    
    List<Statistic> getMinuteStatistics();
      
    List<ComparisonStatistic> getStatisticsForToday();
}
