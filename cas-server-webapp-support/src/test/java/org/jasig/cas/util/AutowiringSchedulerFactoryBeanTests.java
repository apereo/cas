/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.util;

import org.junit.Before;
import org.junit.Test;
import org.quartz.Scheduler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.junit.Assert.assertEquals;

/**
 * Test case for {@link org.jasig.cas.util.AutowiringSchedulerFactoryBean} class.
 *
 * @author Marvin S. Addison
 * @since 3.3.3
 *
 */
public class AutowiringSchedulerFactoryBeanTests {
    private ApplicationContext context;

    private Scheduler scheduler;


    @Before
    public void setUp() throws Exception {
        context = new ClassPathXmlApplicationContext(
        "core-context.xml",
        "applicationContext.xml");

        this.scheduler = (Scheduler) context.getBean("autowiringSchedulerFactoryBean");
        this.scheduler.start();

    }

    @Test
    public void verifyAfterPropertiesSet() throws Exception {
        assertEquals(1, this.scheduler.getTriggerGroupNames().size());
    }
}
