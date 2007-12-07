/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.logging.statistics;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.logging.LogRequest;
import org.jasig.cas.logging.LoggingDao;
import org.jasig.cas.logging.statistics.StatisticsDao.Precision;
import org.jasig.cas.util.CalendarUtils;
import org.jasig.cas.util.annotation.NotEmpty;
import org.jasig.cas.util.annotation.NotNull;
import org.springframework.transaction.annotation.Transactional;

/**
 * Loads the last log requests from the database and attempts to generate statistics from them.
 * <p>
 * Note that this simple implementation assumes that you're processing logs at least once a week.
 * <p>
 * If you do not specify any TimePeriods, it will automatically create one for the entire year.
 *  
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1.2
 *
 */
public final class DefaultStatisticsManagerImpl implements StatisticsManager {
    
    private final Log log = LogFactory.getLog(this.getClass());
    
    @NotNull
    private StatisticsDao statisticsDao;
    
    @NotNull
    private LoggingDao loggingDao;
    
    @NotEmpty
    private List<TimePeriod> timePeriods;
    
    public DefaultStatisticsManagerImpl(final StatisticsDao statisticsDao, final LoggingDao loggingDao) {
        this.statisticsDao = statisticsDao;
        this.loggingDao = loggingDao;
        
        this.timePeriods = new ArrayList<TimePeriod>();
        this.timePeriods.add(new TimePeriod(1, "Entire Year", 1, 1, 12, 31));
    }

    public List<ComparisonStatistic> getStatisticsForToday() {
        final Date today = new Date();
        final List<ComparisonStatistic> comparisonStatistics = new ArrayList<ComparisonStatistic>();
        final List<Statistic> statistics = this.statisticsDao.findAllBy(today);
        
        for (final Statistic statistic : statistics) {
            final ComparisonStatistic comparisonStatistic = new ComparisonStatistic();
            comparisonStatistic.setAverage(statistic.getAverage());
            comparisonStatistic.setEventType(statistic.getEventType());
            comparisonStatistic.setHour(statistic.getHour());
            comparisonStatistic.setMaximum(statistic.getMaximum());
            comparisonStatistic.setMinimum(statistic.getMinimum());
            comparisonStatistic.setMinute(statistic.getMinute());
            comparisonStatistic.setNumberOfPoints(statistic.getNumberOfPoints());
            comparisonStatistic.setTimePeriod(statistic.getTimePeriod());
            comparisonStatistic.setWeekDay(statistic.getWeekDay());
            
            comparisonStatistics.add(comparisonStatistic);
        }
        
        final Calendar cal = CalendarUtils.getCalendarFor(today);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        
        final List<LogRequest> requests = this.loggingDao.retrieveByDateRange(cal.getTime(), today);
        
        for (final LogRequest logRequest : requests) {
            final Calendar calendar = CalendarUtils.getCalendarFor(logRequest.getClientInfo().getRequestDate());
            final int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
            final int hour = calendar.get(Calendar.HOUR_OF_DAY);
            final int minute = calendar.get(Calendar.MINUTE);
            
            for (final ComparisonStatistic cs : comparisonStatistics) {
                final int csWeekDay = cs.getWeekDay();
                final int csHour = cs.getHour();
                final int csMinute = cs.getMinute();
                
                if (((weekDay == csWeekDay && csHour == -1 && csMinute == -1) ||
                (weekDay == csWeekDay && hour == csHour && csMinute == -1) ||
                (weekDay == csWeekDay && hour == csHour && minute == csMinute))
                && logRequest.getEventType().equals(cs.getEventType())) {
                    cs.incrementCurrentValue();
                }
            }
        }

        return comparisonStatistics;
    }

    @Transactional
    public List<Statistic> getDailyStatistics() {
        return this.statisticsDao.getAllDailyStatistics();
    }

    @Transactional
    public List<Statistic> getHourlyStatistics() {
        return this.statisticsDao.getAllHourlyStatistics();
    }

    @Transactional
    public List<Statistic> getMinuteStatistics() {
        return this.statisticsDao.getAllMinuteStatistics();
    }

    @Transactional
    public void processLatestLogRequests() {
        log.trace("Starting processing logs to generate new statistics.");
        
        final Date lastRequest = this.statisticsDao.getDateLastCalculated();
        final List<LogRequest> requests = this.loggingDao.retrieveAllLogRequestsSince(lastRequest);
        
        final List<LogRequest>[] requestsArray = new List[7];
        
        for (int i = 0; i < requestsArray.length; i++) {
            requestsArray[i] = new ArrayList<LogRequest>();
        }
        
        for (final LogRequest request : requests) {
            final Calendar calendar = CalendarUtils.getCalendarFor(request.getClientInfo().getRequestDate());
            final int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            requestsArray[dayOfWeek].add(request);
        }
        
        for (final List<LogRequest> list : requestsArray) {
            processEachDayOfWeek(list);
        }
    }
    
    private void processEachDayOfWeek(final List<LogRequest> requests) {
        log.info("Starting to Process Day of Week.");
        final Map<String, List<LogRequest>> byEventTypes = new HashMap<String, List<LogRequest>>();
        
        for (final LogRequest request : requests) {
            if (!byEventTypes.containsKey(request.getEventType())) {
                byEventTypes.put(request.getEventType(), new ArrayList<LogRequest>());
            }
            
            byEventTypes.get(request.getEventType()).add(request);
        }
        
        for (final Map.Entry<String, List<LogRequest>> entry : byEventTypes.entrySet()) {
            final List<LogRequest> list = entry.getValue();
            saveEntry(list, Precision.DAY);
            processEachHourOfDayForEvent(entry.getValue());
        }
    }
    
    private TimePeriod findTimePeriodFor(final Date date) {
        for (final TimePeriod t : this.timePeriods) {
            if (t.contains(date)) {
                return t;
            }
        }
        
        throw new IllegalArgumentException("No TimePeriod for date: " + date + " found");
    }
    
    private void saveEntry(List<LogRequest> entries, Precision precision) {
        if (entries.isEmpty()) {
            return;
        }
        final LogRequest logRequest = entries.get(0);
        final Date date = logRequest.getClientInfo().getRequestDate();
        final String eventType = logRequest.getEventType();
        final int size = entries.size();
        
        Statistic stat = this.statisticsDao.findBy(date, precision, eventType);
        
        if (stat == null) {
            final Calendar calendar = CalendarUtils.getCalendarFor(date);

            final int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
            final int hour = precision != Precision.DAY ? calendar.get(Calendar.HOUR_OF_DAY) : -1;
            final int minute = precision == Precision.MINUTE ? calendar.get(Calendar.MINUTE) : -1;
            final TimePeriod timePeriod = findTimePeriodFor(date);
            
            stat = new Statistic(timePeriod, weekDay, hour, minute, eventType);
        }
        stat.update(size, date);
        this.statisticsDao.save(stat);      
    }
    
    private void processEachHourOfDayForEvent(final List<LogRequest> entry) {
        log.info("Processing each hour of the day.");
        for (final List<LogRequest> list : constructArrayOfLists(entry, 24, Calendar.HOUR_OF_DAY)) {
            saveEntry(list, Precision.HOUR);
            processEachMinuteOfHourForEvent(list);
        }
    }
    
    private void processEachMinuteOfHourForEvent(final List<LogRequest> list) {
        log.info("Processing each minute of the hour of the day.");
        for (final List<LogRequest> item : constructArrayOfLists(list, 60, Calendar.MINUTE)) {
            saveEntry(item, Precision.MINUTE);
        }
    }
    
    private List<LogRequest>[] constructArrayOfLists(final List<LogRequest> requests, final int sizeOfArray, final int key) {
        final List<LogRequest>[] arrayOfLists = new List[sizeOfArray];
        
        for (int i = 0; i < arrayOfLists.length; i++) {
            arrayOfLists[i] = new ArrayList<LogRequest>();
        }
        
        for (final LogRequest request : requests) {
            final Calendar calendar = CalendarUtils.getCalendarFor(request.getClientInfo().getRequestDate()); 
            final int index = calendar.get(key);
            arrayOfLists[index].add(request);
        }

        return arrayOfLists;
    }
    
    public void setTimePeriods(final List<TimePeriod> timePeriods) {
        this.timePeriods = timePeriods;
    }
}
