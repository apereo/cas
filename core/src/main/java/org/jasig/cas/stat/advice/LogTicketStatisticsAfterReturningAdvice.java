/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.stat.advice;

import java.lang.reflect.Method;
import java.util.Properties;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.stat.TicketStatisticsManager;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.beans.factory.InitializingBean;

/**
 * After returning AOP advice which updates <code>TicketStatistics</code>
 * state at the specific target's object joinpoints.
 * <p>
 * Note: the joinpoints captured by a pointcut are assumed to be the ones that
 * deal with <code>Ticket's</code> state. Those joinpoints (method invocation
 * names) should be properly configured as keys in the <b>statsStateMutators
 * </b> property and must be mapped to the appropriate ticket statistics mutator
 * method names of the <code>TicketStatisticsManager</code>
 * 
 * @author Scott Battaglia
 * @author Dmitriy Kopylenko
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class LogTicketStatisticsAfterReturningAdvice implements
    AfterReturningAdvice, InitializingBean {

    /** The method to monitor for proxy tickets. */
    private static final String PROXY_TICKET_METHOD = "grantServiceTicket";

    /** The mapping of CAS methods to TicketStatistics methods. */
    private Properties statsStateMutators = new Properties();

    /** The TicketStatisticsManager to update the statistics. */
    private TicketStatisticsManager ticketStatsManager;

    /** The registry to monitor. */
    private TicketRegistry ticketRegistry;

    public void afterReturning(final Object returnValue, final Method method,
        final Object[] args, final Object target) throws Throwable {
        if (returnValue == null) {
            return;
        }

        String statsStateMutatorMethodName = this.statsStateMutators
            .getProperty(method.getName());

        if (statsStateMutatorMethodName == null) {
            return;
        }

        if (statsStateMutatorMethodName.equals(PROXY_TICKET_METHOD)) {
            ServiceTicket serviceTicket = (ServiceTicket) this.ticketRegistry
                .getTicket((String) returnValue);

            // we have a proxy ticket!!
            if ((serviceTicket.getGrantingTicket().getAuthentication()
                .getPrincipal() instanceof Service)
                && (serviceTicket.getGrantingTicket().getGrantingTicket() != null)) {
                this.ticketStatsManager.incrementNumberOfProxyTicketsVended();
                return;
            }
        }

        Method statsStateMutatorMethod = this.ticketStatsManager.getClass()
            .getMethod(statsStateMutatorMethodName, null);
        statsStateMutatorMethod.invoke(this.ticketStatsManager, null);
    }

    public void afterPropertiesSet() throws Exception {
        if (this.statsStateMutators == null
            || this.statsStateMutators.isEmpty()) {
            throw new IllegalStateException(
                "You must set the statsStateMutators on "
                    + this.getClass().getName());
        }

        if (this.ticketStatsManager == null) {
            throw new IllegalStateException(
                "You must set the ticketStatsManager bean on "
                    + this.getClass().getName());
        }

        if (this.ticketRegistry == null) {
            throw new IllegalStateException(
                "You must set the ticketRegistry bean on "
                    + this.getClass().getName());
        }
    }

    /**
     * @param ticketStatsManager The TicketStatisticsManager to set.
     */
    public void setTicketStatsManager(
        final TicketStatisticsManager ticketStatsManager) {
        this.ticketStatsManager = ticketStatsManager;
    }

    /**
     * @param statsStateMutators The statsStateMutators to set.
     */
    public void setStatsStateMutators(final Properties statsStateMutators) {
        this.statsStateMutators = statsStateMutators;
    }

    /**
     * @param ticketRegistry the TicketRegistry to set.
     */
    public void setTicketRegistry(final TicketRegistry ticketRegistry) {
        this.ticketRegistry = ticketRegistry;
    }
}
