/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.util;

import junit.framework.TestCase;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.quartz.Scheduler;

/**
 * Test case for {@link org.jasig.cas.util.AutowiringSchedulerFactoryBean} class.
 *
 * @author Marvin S. Addison
 * @version $Revision: $ $Date: $
 * @since 3.3.3
 *
 */
public class AutowiringSchedulerFactoryBeanTests extends TestCase {
    private ApplicationContext context;

    private Scheduler scheduler;


    protected void setUp() throws Exception {
        context = new ClassPathXmlApplicationContext(new String[] {
            "applicationContext.xml"});

        this.scheduler = (Scheduler) context.getBean("autowiringSchedulerFactoryBean");
        this.scheduler.start();

    }

    public void testAfterPropertiesSet() throws Exception {
        assertEquals(1, this.scheduler.getTriggerNames(Scheduler.DEFAULT_GROUP).length);
    }
}
