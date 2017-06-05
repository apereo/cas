package org.apereo.cas.config;

import com.google.common.base.Throwables;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.DefaultMultifactorTriggerSelectionStrategy;
import org.apereo.cas.authentication.MultifactorTriggerSelectionStrategy;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.principal.DefaultWebApplicationResponseBuilderLocator;
import org.apereo.cas.authentication.principal.PersistentIdGenerator;
import org.apereo.cas.authentication.principal.ResponseBuilder;
import org.apereo.cas.authentication.principal.ResponseBuilderLocator;
import org.apereo.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.authentication.principal.WebApplicationServiceResponseBuilder;
import org.apereo.cas.authentication.support.DefaultCasProtocolAttributeEncoder;
import org.apereo.cas.authentication.support.NoOpProtocolAttributeEncoder;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.AbstractResourceBasedServiceRegistryDao;
import org.apereo.cas.services.DefaultServicesManager;
import org.apereo.cas.services.InMemoryServiceRegistry;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.services.RegisteredServicesEventListener;
import org.apereo.cas.services.ServiceRegistryDao;
import org.apereo.cas.services.ServiceRegistryInitializer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.services.DefaultRegisteredServiceCipherExecutor;
import org.apereo.cas.util.services.RegisteredServiceJsonSerializer;
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
import org.springframework.core.io.ClassPathResource;

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

    private static final String BEAN_NAME_SERVICE_REGISTRY_DAO = "serviceRegistryDao";

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @RefreshScope
    @Bean
    public MultifactorTriggerSelectionStrategy defaultMultifactorTriggerSelectionStrategy() {
        final String attributeNameTriggers = casProperties.getAuthn().getMfa().getGlobalPrincipalAttributeNameTriggers();
        final String requestParameter = casProperties.getAuthn().getMfa().getRequestParameter();

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
        return new DefaultWebApplicationResponseBuilderLocator();
    }

    @ConditionalOnMissingBean(name = "webApplicationServiceResponseBuilder")
    @Bean
    public ResponseBuilder<WebApplicationService> webApplicationServiceResponseBuilder() {
        return new WebApplicationServiceResponseBuilder();
    }

    @ConditionalOnMissingBean(name = "casAttributeEncoder")
    @RefreshScope
    @Bean
    public ProtocolAttributeEncoder casAttributeEncoder(@Qualifier("serviceRegistryDao") final ServiceRegistryDao serviceRegistryDao,
                                                        @Qualifier("cacheCredentialsCipherExecutor") final CipherExecutor cacheCredentialsCipherExecutor) {
        return new DefaultCasProtocolAttributeEncoder(servicesManager(serviceRegistryDao),
                registeredServiceCipherExecutor(), cacheCredentialsCipherExecutor);
    }

    @Bean
    public ProtocolAttributeEncoder noOpCasAttributeEncoder() {
        return new NoOpProtocolAttributeEncoder();
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
        return new DefaultServicesManager(serviceRegistryDao);
    }

    @Bean
    @RefreshScope
    public RegisteredServicesEventListener registeredServicesEventListener(@Qualifier("servicesManager") final ServicesManager servicesManager) {
        return new RegisteredServicesEventListener(servicesManager);
    }
    
    @ConditionalOnMissingBean(name = BEAN_NAME_SERVICE_REGISTRY_DAO)
    @Bean
    @RefreshScope
    public ServiceRegistryDao serviceRegistryDao() {
        LOGGER.warn("Runtime memory is used as the persistence storage for retrieving and persisting service definitions. "
                + "Changes that are made to service definitions during runtime WILL be LOST upon container restarts.");

        final List<RegisteredService> services = new ArrayList<>();
        if (applicationContext.containsBean("inMemoryRegisteredServices")) {
            services.addAll(applicationContext.getBean("inMemoryRegisteredServices", List.class));
            LOGGER.debug("Found a list of registered services in the application context. Registering services [{}]", services);
        }
        return new InMemoryServiceRegistry(services);
    }

    @Autowired
    @ConditionalOnMissingBean(name = "jsonServiceRegistryDao")
    @Bean
    public ServiceRegistryInitializer serviceRegistryInitializer(@Qualifier(BEAN_NAME_SERVICE_REGISTRY_DAO) final ServiceRegistryDao serviceRegistryDao) {
        return new ServiceRegistryInitializer(embeddedJsonServiceRegistry(eventPublisher), serviceRegistryDao, servicesManager(serviceRegistryDao),
                casProperties.getServiceRegistry().isInitFromJson());
    }

    @Autowired
    @ConditionalOnMissingBean(name = "jsonServiceRegistryDao")
    @Bean
    public ServiceRegistryDao embeddedJsonServiceRegistry(final ApplicationEventPublisher publisher) {
        try {
            return new EmbeddedServiceRegistryDao(publisher);
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * The embedded service registry that processes built-in JSON service files
     * on the classpath.
     */
    public static class EmbeddedServiceRegistryDao extends AbstractResourceBasedServiceRegistryDao {
        EmbeddedServiceRegistryDao(final ApplicationEventPublisher publisher) throws Exception {
            super(new ClassPathResource("services"), new RegisteredServiceJsonSerializer(), false, publisher);
        }

        @Override
        protected String getExtension() {
            return "json";
        }
    }
}
