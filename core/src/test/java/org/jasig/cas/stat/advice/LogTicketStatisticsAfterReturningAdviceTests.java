/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.stat.advice;

import java.util.Properties;

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
}
