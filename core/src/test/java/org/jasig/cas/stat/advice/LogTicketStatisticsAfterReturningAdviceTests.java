/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.stat.advice;

import java.util.Properties;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.CentralAuthenticationServiceImpl;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.stat.support.TicketStatisticsImpl;
import org.jasig.cas.ticket.registry.DefaultTicketRegistry;
import org.jasig.cas.ticket.registry.TicketRegistry;

import junit.framework.TestCase;

public class LogTicketStatisticsAfterReturningAdviceTests extends TestCase {

    public void testAfterPropertiesSetNoMap() {
        LogTicketStatisticsAfterReturningAdvice advice = new LogTicketStatisticsAfterReturningAdvice();
        try {
            advice.afterPropertiesSet();
            fail("IllegalStateException expected.");
        }
        catch (Exception e) {
            // this is okay
        }
    }

    public void testAfterPropertiesSetEmptyMap() {
        LogTicketStatisticsAfterReturningAdvice advice = new LogTicketStatisticsAfterReturningAdvice();
        try {
            advice.setStatsStateMutators(new Properties());
            advice.afterPropertiesSet();
            fail("IllegalStateException expected.");
        }
        catch (Exception e) {
            // this is okay
        }
    }

    public void testAfterPropertiesSetNoTicketStatsManager() {
        LogTicketStatisticsAfterReturningAdvice advice = new LogTicketStatisticsAfterReturningAdvice();
        Properties properties = new Properties();
        properties.put("test", "test");
        try {
            advice.setStatsStateMutators(properties);
            advice.afterPropertiesSet();
            fail("IllegalStateException expected.");
        }
        catch (Exception e) {
            // this is okay
        }
    }
	
    public void testAfterPropertiesSetNoTicketRegistry() {
        LogTicketStatisticsAfterReturningAdvice advice = new LogTicketStatisticsAfterReturningAdvice();
        Properties properties = new Properties();
        properties.put("test", "test");
        try {
            advice.setStatsStateMutators(properties);
			advice.setTicketStatsManager(new TicketStatisticsImpl());
            advice.afterPropertiesSet();
            fail("IllegalStateException expected.");
        }
        catch (Exception e) {
            // this is okay
        }
    }
	
    public void testAfterPropertiesWorksOkay() {
        LogTicketStatisticsAfterReturningAdvice advice = new LogTicketStatisticsAfterReturningAdvice();
        Properties properties = new Properties();
        properties.put("test", "test");
        try {
            advice.setStatsStateMutators(properties);
			advice.setTicketStatsManager(new TicketStatisticsImpl());
			advice.setTicketRegistry(new DefaultTicketRegistry());
            advice.afterPropertiesSet();
        }
        catch (Exception e) {
			fail("Exception unexpected.");
        }
    }
	
	public void testNullReturnsOkay() {
		LogTicketStatisticsAfterReturningAdvice advice = new LogTicketStatisticsAfterReturningAdvice();
		try {
			advice.afterReturning(null, null, null, null);
		} catch (Throwable e) {
			fail("Throwable not expected.");
		}
	}
	
	public void testMethodNotFound() {
		LogTicketStatisticsAfterReturningAdvice advice = new LogTicketStatisticsAfterReturningAdvice();
        Properties properties = new Properties();
        properties.put("test", "test");
		
		advice.setStatsStateMutators(properties);
		try {
			
			advice.afterReturning(null, advice.getClass().getDeclaredMethods()[0], null, null);
		} catch (Throwable e) {
			fail("Throwable not expected.");
		}
	}
	
	public void testMethodFound() {
		TicketStatisticsImpl t = new TicketStatisticsImpl();
		LogTicketStatisticsAfterReturningAdvice advice = new LogTicketStatisticsAfterReturningAdvice();
		advice.setTicketRegistry(new DefaultTicketRegistry());
		advice.setTicketStatsManager(t);
		Properties p = new Properties();
		p.put("createTicketGrantingTicket", "incrementNumberOfTicketGrantingTicketsVended");
		advice.setStatsStateMutators(p);
		
		try {
			
			advice.afterReturning("tgt", CentralAuthenticationService.class.getMethod("createTicketGrantingTicket", new Class[] {Credentials.class}), null, new CentralAuthenticationServiceImpl());
			assertEquals(1, t.getNumberOfTicketGrantingTicketsVended());
		} catch (Throwable e) {
			e.printStackTrace();
			fail("Throwable not expected.");
		}
	}
}
