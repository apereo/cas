/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.logging.statistics;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.jasig.cas.util.CalendarUtils;
import org.springframework.orm.jpa.support.JpaDaoSupport;

/**
 * Jpa-backed implementation of the StatisticsDao.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1.2
 *
 */
public final class JpaStatisticsDaoImpl extends JpaDaoSupport implements StatisticsDao {

    public Statistic findBy(final Date date, final Precision precision, final String eventType) {
        final Calendar c = CalendarUtils.getCalendarFor(date);
        final int weekDay = c.get(Calendar.DAY_OF_WEEK);
        final int hour = precision != Precision.DAY ? c.get(Calendar.HOUR_OF_DAY) : -1;
        final int minute = precision == Precision.MINUTE ? c.get(Calendar.MINUTE) : -1;
        
        final List<Statistic> statistics = getJpaTemplate().find("select s from Statistic s where s.eventType = ?1 and s.weekDay = ?2 and s.hour = ?3 and s.minute = ?4", eventType, weekDay, hour, minute);     
        for (final Statistic statistic : statistics) {
            if (statistic.getTimePeriod().contains(date)) {
                return statistic;
            }
        }
        
        return null;
    }

    public List<Statistic> findAllBy(final Date date) {
        final int weekDay = CalendarUtils.getCurrentDayOfWeekFor(date);
        final List<Statistic> stats = getJpaTemplate().find("select s from Statistic s where s.weekDay = ?1 Order By s.hour, s.minute, s.eventType", weekDay);
        final List<Statistic> restricted = new ArrayList<Statistic>();
        
        for (final Statistic stat : stats) {
            if (stat.getTimePeriod().contains(date) ) {
                restricted.add(stat);
            }
        }
        
        return restricted;
    }

    public List<Statistic> getAllDailyStatistics() {
        return getJpaTemplate().find("select s from Statistic s where s.hour = -1 and s.minute = -1 order by s.timePeriod.endMonth DESC, s.timePeriod.endDay DESC, s.weekDay ASC, s.eventType");
    }

    public List<Statistic> getAllHourlyStatistics() {
        return getJpaTemplate().find("select s from Statistic s where s.hour > -1 and s.minute = -1 order by s.timePeriod.endMonth DESC, s.timePeriod.endDay DESC, s.weekDay ASC, s.hour ASC, s.eventType");
    }

    public List<Statistic> getAllMinuteStatistics() {
        return getJpaTemplate().find("select s from Statistic s where s.hour > -1 and s.minute > -1 order by s.timePeriod.endMonth DESC, s.timePeriod.endDay DESC, s.weekDay ASC, s.hour ASC, s.minute ASC, s.eventType");
    }

    public void save(final Statistic statistic) {
        getJpaTemplate().persist(statistic);
    }

    public Date getDateLastCalculated() {
        final Query query = getJpaTemplate().getEntityManagerFactory().createEntityManager().createQuery("select s from Statistic s Order By s.lastUpdate DESC");
        query.setMaxResults(1);
        
        final List<Statistic> list = query.getResultList();
       
        if (list.isEmpty()) {
            return null;
        }
        
        return list.get(0).getLastUpdate();
    }
}
