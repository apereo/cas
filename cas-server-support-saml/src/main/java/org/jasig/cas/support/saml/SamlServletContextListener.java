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

package org.jasig.cas.support.saml;

import org.jasig.cas.web.support.ArgumentExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.WebListener;

import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Initializes the CAS root servlet context to make sure
 * SAML validation endpoint can be activated by the main CAS servlet.
 * @author Misagh Moayyed
 * @since 4.2
 */
@WebListener
@Component
public class SamlServletContextListener implements ServletContextListener, ApplicationContextAware {
    private static final String CAS_SERVLET_NAME = "cas";

    private final Logger logger = getLogger(this.getClass());

    @Autowired
    @Qualifier("samlArgumentExtractor")
    private ArgumentExtractor samlArgumentExtractor;

    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        logger.info("Initializing SAML servlet context...");

        final ServletRegistration registration = sce.getServletContext().getServletRegistration(CAS_SERVLET_NAME);
        registration.addMapping(SamlProtocolConstants.ENDPOINT_SAML_VALIDATE);

        logger.info("Added [{}] to {} servlet context", SamlProtocolConstants.ENDPOINT_SAML_VALIDATE, CAS_SERVLET_NAME);
    }

    @Override
    public void contextDestroyed(final ServletContextEvent sce) {}

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        if (applicationContext.getParent() == null) {
            final List<ArgumentExtractor> list = applicationContext.getBean("argumentExtractors", List.class);
            list.add(this.samlArgumentExtractor);
        }
    }

}
