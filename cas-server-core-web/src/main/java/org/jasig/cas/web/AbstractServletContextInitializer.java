package org.jasig.cas.web;

import org.jasig.cas.authentication.AuthenticationHandler;
import org.jasig.cas.authentication.AuthenticationMetaDataPopulator;
import org.jasig.cas.authentication.principal.PrincipalResolver;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.ServiceFactory;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ReloadableServicesManager;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.ticket.UniqueTicketIdGenerator;
import org.jasig.cas.web.support.ArgumentExtractor;
import org.jasig.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import java.util.List;
import java.util.Map;

/**
 * Parent class for all servlet context initializers
 * that provides commons methods for retrieving beans
 * from the context dynamically.
 * @author Misagh Moayyed
 * @since 4.2
 */
@Component
public abstract class AbstractServletContextInitializer
    implements ApplicationContextInitializer<ConfigurableApplicationContext>,
    WebApplicationInitializer,
    ServletContextListener, ApplicationContextAware {

    protected final transient Logger logger = LoggerFactory.getLogger(this.getClass());

    /** Application context. */
    protected ApplicationContext applicationContext;

    private final String contextInitializerName = getClass().getSimpleName();

    /**
     * Instantiates a new servlet context initializer.
     */
    protected AbstractServletContextInitializer() {}


    @Override
    public final void contextInitialized(final ServletContextEvent sce) {
        logger.info("Initializing {} context...", contextInitializerName);
        initializeServletContext(sce);
        logger.info("Initialized {} context...", contextInitializerName);
    }

    @Override
    public final void contextDestroyed(final ServletContextEvent sce) {
        logger.info("Destroying {} context...", contextInitializerName);
        destroyServletContext(sce);
        logger.info("Destroyed {} context...", contextInitializerName);
    }

    @Override
    public final void initialize(final ConfigurableApplicationContext configurableApplicationContext) {
        logger.info("Initializing application context...");
        initializeApplicationContext(configurableApplicationContext);
    }

    @Override
    public final void onStartup(final ServletContext servletContext) throws ServletException {
        logger.info("Starting up servlet application context...");
        onStartupServletContext(servletContext);
    }

    /**
     * On startup servlet context.
     *
     * @param servletContext the servlet context
     */
    protected void onStartupServletContext(final ServletContext servletContext) {}

    /**
     * Instantiates a new Initialize application context.
     *
     * @param configurableApplicationContext the configurable application context
     */
    protected void initializeApplicationContext(final ConfigurableApplicationContext configurableApplicationContext) {}

    @Override
    public final void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;

        try {
            logger.info("Initializing {} root application context", contextInitializerName);
            initializeRootApplicationContext();
            logger.info("Initialized {} root application context successfully", contextInitializerName);

            logger.info("Initializing {} servlet application context", contextInitializerName);
            initializeServletApplicationContext();
            logger.info("Initialized {} servlet application context successfully", contextInitializerName);
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
        logger.debug("Adding {} and {} to application context", handler, resolver);
        final Map<AuthenticationHandler, PrincipalResolver> authenticationHandlersResolvers =
                applicationContext.getBean("authenticationHandlersResolvers", Map.class);
        authenticationHandlersResolvers.put(handler, resolver);
    }

    /**
     * Add authentication handler without any principal resolver.
     *
     * @param handler the handler
     */
    protected final void addAuthenticationHandler(final AuthenticationHandler handler) {
        addAuthenticationHandlerPrincipalResolver(handler, null);
    }

    /**
     * Add authentication metadata populator.
     * @param populator the populator
     */
    protected final void addAuthenticationMetadataPopulator(final AuthenticationMetaDataPopulator populator) {
        logger.debug("Adding {} to application context", populator);
        final List<AuthenticationMetaDataPopulator> authenticationMetadataPopulators =
            applicationContext.getBean("authenticationMetadataPopulators", List.class);
        authenticationMetadataPopulators.add(populator);
    }

    protected final ConfigurableEnvironment getEnvironment() {
        return (ConfigurableEnvironment) this.applicationContext.getEnvironment();
    }

    /**
     * Gets cas servlet registration.
     *
     * @param sce the sce
     * @return the cas servlet registration
     */
    protected final ServletRegistration getCasServletRegistration(final ServletContextEvent sce) {
        final ServletRegistration registration = sce.
            getServletContext().getServletRegistration(WebUtils.CAS_SERVLET_NAME);
        if (registration == null) {
            logger.debug("Servlet [{}] is not registered with this context", WebUtils.CAS_SERVLET_NAME);
        }
        return registration;
    }


    /**
     * Add registered service to services manager.
     *
     * @param svc the svc
     */
    protected final void addRegisteredServiceToServicesManager(final RegisteredService svc) {
        logger.debug("Adding {} to application context services", svc);
        final ServicesManager manager = getServicesManager();
        manager.save(svc);
    }

    protected final ReloadableServicesManager getServicesManager() {
        return this.applicationContext.getBean("servicesManager", ReloadableServicesManager.class);
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
    protected final void addControllerToCasServletHandlerMapping(final String path, final Object controller) {
        logger.debug("Adding {} to application context for {}", controller, path);
        final SimpleUrlHandlerMapping handlerMappingC = getCasServletHandlerMapping();
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
        logger.info("Adding [{}] to {} servlet context", mapping, WebUtils.CAS_SERVLET_NAME);
        final ServletRegistration registration = getCasServletRegistration(sce);
        if (registration != null) {

            registration.addMapping(mapping);
            logger.info("Added [{}] to {} servlet context", mapping, WebUtils.CAS_SERVLET_NAME);
        }
    }

    /**
     * Add argument extractor.
     *
     * @param ext the ext
     */
    protected final void addArgumentExtractor(final ArgumentExtractor ext) {
        logger.debug("Adding [{}] application context", ext);
        final List<ArgumentExtractor> list = applicationContext.getBean("argumentExtractors", List.class);
        list.add(ext);
    }


    /**
     * Add service factory.
     *
     * @param factory the factory
     */
    protected void addServiceFactory(final ServiceFactory<? extends Service> factory) {
        logger.debug("Adding [{}] application context", factory);
        final List<ServiceFactory<? extends Service>> list =
                applicationContext.getBean("serviceFactoryList", List.class);
        list.add(factory);
    }

    /**
     * Add service ticket unique id generator.
     *
     * @param serviceName the service name
     * @param gen the gen
     */
    protected final void addServiceTicketUniqueIdGenerator(final String serviceName,
                                                           final UniqueTicketIdGenerator gen) {
        logger.debug("Adding [{}] for {} application context", serviceName, gen);
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
