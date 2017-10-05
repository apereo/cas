package org.apereo.cas.config;

import org.apereo.cas.authentication.DefaultMultifactorTriggerSelectionStrategy;
import org.apereo.cas.authentication.MultifactorTriggerSelectionStrategy;
import org.apereo.cas.authentication.principal.DefaultWebApplicationResponseBuilderLocator;
import org.apereo.cas.authentication.principal.PersistentIdGenerator;
import org.apereo.cas.authentication.principal.ResponseBuilder;
import org.apereo.cas.authentication.principal.ResponseBuilderLocator;
import org.apereo.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.authentication.principal.WebApplicationServiceResponseBuilder;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProperties;
import org.apereo.cas.services.DefaultServicesManager;
import org.apereo.cas.services.DomainServicesManager;
import org.apereo.cas.services.InMemoryServiceRegistry;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.services.RegisteredServicesEventListener;
import org.apereo.cas.services.ServiceRegistryDao;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.util.DefaultRegisteredServiceCipherExecutor;
import org.apereo.cas.util.io.CommunicationsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link CasCoreServicesConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreServicesConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCoreServicesConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasCoreServicesConfiguration.class);

    @Autowired
    @Qualifier("communicationsManager")
    private CommunicationsManager communicationsManager;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ApplicationContext applicationContext;

    @RefreshScope
    @Bean
    public MultifactorTriggerSelectionStrategy defaultMultifactorTriggerSelectionStrategy() {

        final MultifactorAuthenticationProperties mfa = casProperties.getAuthn().getMfa();
        final String attributeNameTriggers = mfa.getGlobalPrincipalAttributeNameTriggers();
        final String requestParameter = mfa.getRequestParameter();

        return new DefaultMultifactorTriggerSelectionStrategy(attributeNameTriggers, requestParameter);
    }

    @RefreshScope
    @Bean
    public PersistentIdGenerator shibbolethCompatiblePersistentIdGenerator() {
        return new ShibbolethCompatiblePersistentIdGenerator();
    }

    @ConditionalOnMissingBean(name = "webApplicationResponseBuilderLocator")
    @Bean
    public ResponseBuilderLocator webApplicationResponseBuilderLocator() {
        return new DefaultWebApplicationResponseBuilderLocator(applicationContext);
    }

    @ConditionalOnMissingBean(name = "webApplicationServiceResponseBuilder")
    @Bean
    public ResponseBuilder<WebApplicationService> webApplicationServiceResponseBuilder() {
        return new WebApplicationServiceResponseBuilder();
    }

    @ConditionalOnMissingBean(name = "registeredServiceCipherExecutor")
    @Bean
    @RefreshScope
    public RegisteredServiceCipherExecutor registeredServiceCipherExecutor() {
        return new DefaultRegisteredServiceCipherExecutor();
    }

    @ConditionalOnMissingBean(name = "servicesManager")
    @Bean
    @RefreshScope
    public ServicesManager servicesManager(@Qualifier("serviceRegistryDao") final ServiceRegistryDao serviceRegistryDao) {
        switch (casProperties.getServiceRegistry().getManagementType()) {
            case DOMAIN:
                LOGGER.debug("Managing CAS service definitions via domains");
                return new DomainServicesManager(serviceRegistryDao, eventPublisher);
            case DEFAULT:
            default:
                break;
        }
        return new DefaultServicesManager(serviceRegistryDao, eventPublisher);
    }

    @Bean
    @RefreshScope
    public RegisteredServicesEventListener registeredServicesEventListener(@Qualifier("servicesManager") final ServicesManager servicesManager) {
        return new RegisteredServicesEventListener(servicesManager, casProperties, communicationsManager);
    }

    @ConditionalOnMissingBean(name = "serviceRegistryDao")
    @Bean
    @RefreshScope
    public ServiceRegistryDao serviceRegistryDao() {
        LOGGER.warn("Runtime memory is used as the persistence storage for retrieving and persisting service definitions. "
                + "Changes that are made to service definitions during runtime WILL be LOST upon container restarts. "
                + "Ideally for production, you need to choose a storage option (JDBC, etc) to store and track service definitions.");

        final List<RegisteredService> services = new ArrayList<>();
        if (applicationContext.containsBean("inMemoryRegisteredServices")) {
            services.addAll(applicationContext.getBean("inMemoryRegisteredServices", List.class));
            LOGGER.debug("Found a list of registered services in the application context. Registering services [{}]", services);
        }
        return new InMemoryServiceRegistry(services);
    }
}
