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

package org.jasig.cas.support.wsfederation;


import org.jasig.cas.authentication.AuthenticationHandler;
import org.jasig.cas.authentication.principal.PrincipalResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.Map;

/**
 * Initializes the CAS root servlet context to make sure
 * ADFS validation can be activated and authentication handlers injected.
 * @author Misagh Moayyed
 * @since 4.2
 */
@WebListener
@Component
public class WsFedServletContextListener implements ServletContextListener, ApplicationContextAware {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("adfsAuthNHandler")
    private AuthenticationHandler adfsAuthNHandler;

    @Autowired
    @Qualifier("adfsPrincipalResolver")
    private PrincipalResolver adfsPrincipalResolver;


    @Autowired
    @Qualifier("authenticationHandlersResolvers")
    private Map authenticationHandlersResolvers;

    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        logger.info("Initializing WsFed servlet context...");
    }

    @Override
    public void contextDestroyed(final ServletContextEvent sce) {}

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        try {
            if (applicationContext.getParent() == null) {
                logger.info("Initializing WsFed root application context");

                authenticationHandlersResolvers.put(adfsAuthNHandler, adfsPrincipalResolver);

                logger.info("Initialized WsFed root application context successfully");
            } else {
                logger.info("Initializing WsFed application context");

                logger.info("Initialized WsFed application context successfully");
            }
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
