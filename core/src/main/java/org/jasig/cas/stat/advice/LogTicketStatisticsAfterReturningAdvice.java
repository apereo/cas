/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.stat.advice;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.jasig.cas.stat.TicketStats;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.beans.factory.InitializingBean;


/**
 * @author Scott Battaglia
 * @author Dmitriy Kopylenko
 * @version $Id$
 *
 */
public class LogTicketStatisticsAfterReturningAdvice implements AfterReturningAdvice, InitializingBean {
    private Map statsStateMutators = new HashMap();

    private TicketStats ticketStats;
    /**
     * @see org.springframework.aop.AfterReturningAdvice#afterReturning(java.lang.Object, java.lang.reflect.Method, java.lang.Object[], java.lang.Object)
     */
    public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
        if (returnValue == null) {
            return;
        }
        
        String statsStateMutatorMethodName = (String)this.statsStateMutators.get(method.getName());
        if(statsStateMutatorMethodName == null){
            return;
        }
        Method statsStateMutatorMethod = this.ticketStats.getClass().getMethod(statsStateMutatorMethodName, null);
        statsStateMutatorMethod.invoke(this.ticketStats, null);
    }
    
    /**
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        if (this.statsStateMutators == null | this.statsStateMutators.isEmpty()) {
            throw new IllegalStateException("You must set the statsStateMutators on " + this.getClass().getName());
        }
        
        if (this.ticketStats == null ) {
            throw new IllegalStateException("You must set the ticketStats bean on " + this.getClass().getName());
        }
    }

    /**
     * @param ticketStats The ticketStats to set.
     */
    public void setTicketStats(TicketStats ticketStats) {
        this.ticketStats = ticketStats;
    }
    
    /**
     * @param statsStateMutators The statsStateMutators to set.
     */
    public void setStatsStateMutators(Map statsStateMutators) {
        this.statsStateMutators = statsStateMutators;
    }
}
