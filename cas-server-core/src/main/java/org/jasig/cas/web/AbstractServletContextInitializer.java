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

package org.jasig.cas.web;

import org.jasig.cas.authentication.AuthenticationHandler;
import org.jasig.cas.authentication.principal.PrincipalResolver;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.util.UniqueTicketIdGenerator;
import org.jasig.cas.web.support.ArgumentExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.WebListener;
import java.util.List;
import java.util.Map;

/**
 * Parent class for all servlet context initializers
 * that provides commons methods for retrieving beans
 * from the context dynamically.
 * @author Misagh Moayyed
 * @since 4.2
 */
@WebListener
@Component
public abstract class AbstractServletContextInitializer implements ServletContextListener, ApplicationContextAware {

    /** Default CAS Servlet name. **/
    private static final String CAS_SERVLET_NAME = "cas";

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** Application context. */
    protected ApplicationContext applicationContext;

    @Override
    public final void contextInitialized(final ServletContextEvent sce) {
        logger.info("Initializing {} context...", getClass().getSimpleName());
        initializeServletContext(sce);
        logger.info("Initialized {} context...", getClass().getSimpleName());
    }

    @Override
    public final void contextDestroyed(final ServletContextEvent sce) {
        logger.info("Destroying {} context...", getClass().getSimpleName());
        destroyServletContext(sce);
        logger.info("Destroyed {} context...", getClass().getSimpleName());
    }

    @Override
    public final void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;

        try {
            if (applicationContext.getParent() == null) {
                logger.info("Initializing Saml root application context");
                initializeRootApplicationContext();
                logger.info("Initialized Saml root application context successfully");
            } else {
                logger.info("Initializing Saml application context");
                initializeServletApplicationContext();
                logger.info("Initialized Saml application context successfully");
            }
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Add authentication handler principal resolver.
     *
     * @param handler the handler
     * @param resolver the resolver
     */
    protected final void addAuthenticationHandlerPrincipalResolver(final AuthenticationHandler handler,
                                                              final PrincipalResolver resolver) {
        final Map<AuthenticationHandler, PrincipalResolver> authenticationHandlersResolvers =
                applicationContext.getBean("authenticationHandlersResolvers", Map.class);
        authenticationHandlersResolvers.put(handler, resolver);
    }

    /**
     * Gets cas servlet registration.
     *
     * @param sce the sce
     * @return the cas servlet registration
     */
    protected final ServletRegistration getCasServletRegistration(final ServletContextEvent sce) {
        final ServletRegistration registration = sce.getServletContext().getServletRegistration(CAS_SERVLET_NAME);
        return registration;
    }


    /**
     * Add registered service to services manager.
     *
     * @param svc the svc
     */
    protected final void addRegisteredServiceToServicesManager(final RegisteredService svc) {
        final ServicesManager manager = this.applicationContext.getBean("servicesManager", ServicesManager.class);

    }

    /**
     * Gets cas servlet handler mapping.
     *
     * @return the cas servlet handler mapping
     */
    protected final SimpleUrlHandlerMapping getCasServletHandlerMapping() {
        final SimpleUrlHandlerMapping handlerMappingC =
                applicationContext.getBean("handlerMappingC", SimpleUrlHandlerMapping.class);
        return handlerMappingC;
    }

    /**
     * Add controller to cas servlet handler mapping.
     *
     * @param path the path
     * @param controller the controller
     */
    protected final void addControllerToCasServletHandlerMapping(final String path, final Controller controller) {
        final SimpleUrlHandlerMapping handlerMappingC =
                applicationContext.getBean(SimpleUrlHandlerMapping.class);
        final Map<String, Object> urlMap = (Map<String, Object>) handlerMappingC.getUrlMap();
        urlMap.put(path, controller);
        handlerMappingC.initApplicationContext();

    }

    /**
     * Add controller to cas servlet handler mapping.
     *
     * @param path the path
     * @param controller the controller
     */
    protected final void addControllerToCasServletHandlerMapping(final String path, final String controller) {
        addControllerToCasServletHandlerMapping(path, getController(controller));
    }

    /**
     * Gets controller.
     *
     * @param id the id
     * @return the controller
     */
    protected final Controller getController(final String id) {
        return applicationContext.getBean(id, Controller.class);
    }

    /**
     * Add endpoint mapping to cas servlet.
     *
     * @param sce the sce
     * @param mapping the mapping
     */
    protected final void addEndpointMappingToCasServlet(final ServletContextEvent sce, final String mapping) {
        final ServletRegistration registration = getCasServletRegistration(sce);
        registration.addMapping(mapping);
        logger.info("Added [{}] to {} servlet context", mapping, CAS_SERVLET_NAME);
    }

    /**
     * Add argument extractor.
     *
     * @param ext the ext
     */
    protected final void addArgumentExtractor(final ArgumentExtractor ext) {
        final List<ArgumentExtractor> list = applicationContext.getBean("argumentExtractors", List.class);
        list.add(ext);
    }

    /**
     * Add service ticket unique id generator.
     *
     * @param serviceName the service name
     * @param gen the gen
     */
    protected final void addServiceTicketUniqueIdGenerator(final String serviceName, final UniqueTicketIdGenerator gen) {
        final Map<String, UniqueTicketIdGenerator> map =
                applicationContext.getBean("uniqueIdGeneratorsMap", Map.class);
        map.put(serviceName, gen);
    }

    /**
     * Initialize root application context.
     */
    protected void initializeRootApplicationContext() {}

    /**
     * Initialize servlet application context.
     */
    protected void initializeServletApplicationContext() {}

    /**
     * Initialize servlet context.
     *
     * @param event the event
     */
    protected void initializeServletContext(final ServletContextEvent event) {}

    /**
     * Destroy servlet context.
     *
     * @param event the event
     */
    protected void destroyServletContext(final ServletContextEvent event) {}
}
