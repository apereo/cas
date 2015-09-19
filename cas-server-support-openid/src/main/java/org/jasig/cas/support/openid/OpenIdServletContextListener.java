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

package org.jasig.cas.support.openid;

import org.jasig.cas.authentication.AuthenticationHandler;
import org.jasig.cas.authentication.principal.PrincipalResolver;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.support.openid.authentication.principal.OpenIdService;
import org.jasig.cas.util.UniqueTicketIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.WebListener;
import java.util.Map;

/**
 * Initializes the CAS root servlet context to make sure
 * OpenID endpoint can be activated by the main CAS servlet.
 * @author Misagh Moayyed
 * @since 4.2
 */
@WebListener
@Component
public class OpenIdServletContextListener implements ServletContextListener, ApplicationContextAware {

    private static final String CAS_SERVLET_NAME = "cas";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("serviceTicketUniqueIdGenerator")
    private UniqueTicketIdGenerator serviceTicketUniqueIdGenerator;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("openIdCredentialsAuthenticationHandler")
    private AuthenticationHandler openIdCredentialsAuthenticationHandler;

    @Autowired
    @Qualifier("openIdPrincipalResolver")
    private PrincipalResolver openIdPrincipalResolver;

    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        logger.info("Initializing OAuth servlet context...");
        final ServletRegistration registration = sce.getServletContext().getServletRegistration(CAS_SERVLET_NAME);
        registration.addMapping(OpenIdConstants.ENDPOINT_OPENID);
        logger.info("Added [{}] to {} servlet context", OpenIdConstants.ENDPOINT_OPENID, CAS_SERVLET_NAME);
    }

    @Override
    public void contextDestroyed(final ServletContextEvent sce) {}

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        try {
            if (applicationContext.getParent() != null) {
                logger.info("Initializing OpenID application context");

                final SimpleUrlHandlerMapping handlerMappingC = applicationContext.getBean(SimpleUrlHandlerMapping.class);
                final Controller controller = applicationContext.getBean("openIdProviderController",
                        Controller.class);
                final Map<String, Object> urlMap = (Map<String, Object>) handlerMappingC.getUrlMap();
                urlMap.put(OpenIdConstants.ENDPOINT_OPENID, controller);
                handlerMappingC.initApplicationContext();

                final Map<String, UniqueTicketIdGenerator> map =
                        applicationContext.getBean("uniqueIdGeneratorsMap", Map.class);
                map.put(OpenIdService.class.getCanonicalName(), this.serviceTicketUniqueIdGenerator);


                logger.info("Initialized OpenID application context successfully");
            } else {
                logger.info("Initializing OpenID root application context");
                final Map<AuthenticationHandler, PrincipalResolver> authenticationHandlersResolvers =
                        applicationContext.getBean("authenticationHandlersResolvers", Map.class);
                authenticationHandlersResolvers.put(openIdCredentialsAuthenticationHandler, openIdPrincipalResolver);
                logger.info("Initialized OpenID root application context successfully");
            }
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
