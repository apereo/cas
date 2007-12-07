/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.logging.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.logging.statistics.StatisticsManager;
import org.jasig.cas.util.CalendarUtils;
import org.jasig.cas.util.annotation.NotNull;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * Returns the statistics generated so they can be viewed.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1.2
 *
 */
public final class StatisticsViewController extends AbstractController {

    @NotNull
    private final StatisticsManager statisticsManager;
    
    public StatisticsViewController(final StatisticsManager statisticsManager) {
        this.statisticsManager = statisticsManager;
    }
    
    protected ModelAndView handleRequestInternal(final HttpServletRequest request,
        HttpServletResponse response) throws Exception {
        final ModelAndView modelAndView = new ModelAndView("statisticsView");
        modelAndView.addObject("statisticsByDay", this.statisticsManager.getDailyStatistics());
        modelAndView.addObject("statisticsByHour", this.statisticsManager.getHourlyStatistics());
        modelAndView.addObject("statisticsByMinute", this.statisticsManager.getMinuteStatistics());
        modelAndView.addObject("pageTitle", modelAndView.getViewName());
        modelAndView.addObject("weekdays", CalendarUtils.WEEKDAYS);
        
        return modelAndView;
    }
}
