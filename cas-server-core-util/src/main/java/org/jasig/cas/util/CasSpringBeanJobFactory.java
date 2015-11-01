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

import org.quartz.spi.TriggerFiredBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import javax.validation.constraints.NotNull;

/**
 * Creates quartz job, and autowires them based on the application context.
 * @author Misagh Moayyed
 * @since 4.2
 */

public class CasSpringBeanJobFactory extends SpringBeanJobFactory {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @NotNull
    private final ApplicationContext applicationContext;

    /**
     * Instantiates a new Cas spring bean job factory.
     *
     * @param applicationContext the application context
     */
    public CasSpringBeanJobFactory(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    protected Object createJobInstance(final TriggerFiredBundle bundle) throws Exception {
        final AutowireCapableBeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();
        final Object job = super.createJobInstance(bundle);
        logger.debug("Created job {} for bundle {}", job, bundle);
        beanFactory.autowireBean(job);
        logger.debug("Autowired job per the application context");
        return job;
    }
}
