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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Parent class for all servlet context initializers
 * that provides commons methods for retrieving beans
 * from the context dynamically.
 * @author Misagh Moayyed
 * @since 4.2
 */
@RefreshScope
@Component
public abstract class BaseApplicationContextWrapper implements ApplicationContextAware {

    protected transient Logger logger = LoggerFactory.getLogger(this.getClass());

    /** Application context. */
    protected ApplicationContext applicationContext;

    /**
     * Instantiates a new servlet context initializer.
     */
    protected BaseApplicationContextWrapper() {}
    
    /**
     * Add authentication handler principal resolver.
     *
     * @param handler the handler
     * @param resolver the resolver
     */
    protected void addAuthenticationHandlerPrincipalResolver(final AuthenticationHandler handler,
                                                              final PrincipalResolver resolver) {
        logger.debug("Adding {} and {} to application context", handler, resolver);
        final Map<AuthenticationHandler, PrincipalResolver> authenticationHandlersResolvers =
                this.applicationContext.getBean("authenticationHandlersResolvers", Map.class);
        authenticationHandlersResolvers.put(handler, resolver);
    }

    /**
     * Add authentication handler without any principal resolver.
     *
     * @param handler the handler
     */
    protected void addAuthenticationHandler(final AuthenticationHandler handler) {
        addAuthenticationHandlerPrincipalResolver(handler, null);
    }

    /**
     * Add authentication metadata populator.
     * @param populator the populator
     */
    protected void addAuthenticationMetadataPopulator(final AuthenticationMetaDataPopulator populator) {
        addAuthenticationMetadataPopulator(populator, 0);
    }

    /**
     * Add authentication metadata populator.
     *
     * @param populator the populator
     * @param index     the index
     */
    protected void addAuthenticationMetadataPopulator(final AuthenticationMetaDataPopulator populator, final int index) {
        logger.debug("Adding {} to application context", populator);
        final List<AuthenticationMetaDataPopulator> authenticationMetadataPopulators =
                this.applicationContext.getBean("authenticationMetadataPopulators", List.class);
        authenticationMetadataPopulators.add(index, populator);
    }
    

    /**
     * Add registered service to services manager.
     *
     * @param svc the svc
     */
    protected void addRegisteredServiceToServicesManager(final RegisteredService svc) {
        logger.debug("Adding {} to application context services", svc);
        final ServicesManager manager = getServicesManager();
        manager.save(svc);
    }

    protected ReloadableServicesManager getServicesManager() {
        return this.applicationContext.getBean("servicesManager", ReloadableServicesManager.class);
    }
        
    /**
     * Add service factory.
     *
     * @param factory the factory
     */
    protected void addServiceFactory(final ServiceFactory<? extends Service> factory) {
        addServiceFactory(factory, 0);
    }

    /**
     * Add service factory.
     *
     * @param factory the factory
     * @param index   the index
     */
    protected void addServiceFactory(final ServiceFactory<? extends Service> factory, final int index) {
        logger.debug("Adding [{}] application context", factory);
        final List<ServiceFactory<? extends Service>> list =
                this.applicationContext.getBean("serviceFactoryList", List.class);
        list.add(index, factory);
    }

    /**
     * Add service ticket unique id generator.
     *
     * @param serviceName the service name
     * @param gen the gen
     */
    protected void addServiceTicketUniqueIdGenerator(final String serviceName,
                                                           final UniqueTicketIdGenerator gen) {
        logger.debug("Adding [{}] for {} application context", serviceName, gen);
        final Map<String, UniqueTicketIdGenerator> map =
                this.applicationContext.getBean("uniqueIdGeneratorsMap", Map.class);
        map.put(serviceName, gen);
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
