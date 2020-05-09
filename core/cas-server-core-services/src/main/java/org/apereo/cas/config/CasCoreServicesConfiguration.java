package org.apereo.cas.config;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.DefaultWebApplicationResponseBuilderLocator;
import org.apereo.cas.authentication.principal.PersistentIdGenerator;
import org.apereo.cas.authentication.principal.ResponseBuilder;
import org.apereo.cas.authentication.principal.ResponseBuilderLocator;
import org.apereo.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.authentication.principal.WebApplicationServiceResponseBuilder;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ChainingServiceRegistry;
import org.apereo.cas.services.ChainingServicesManager;
import org.apereo.cas.services.DefaultChainingServiceRegistry;
import org.apereo.cas.services.DefaultServiceRegistryExecutionPlan;
import org.apereo.cas.services.DefaultServicesManager;
import org.apereo.cas.services.ImmutableServiceRegistry;
import org.apereo.cas.services.InMemoryServiceRegistry;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyAuditableEnforcer;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.services.RegisteredServicePublicKeyCipherExecutor;
import org.apereo.cas.services.RegisteredServicesEventListener;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlan;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServiceRegistryListener;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.ServicesManagerExecutionPlanConfigurer;
import org.apereo.cas.services.ServicesManagerScheduledLoader;
import org.apereo.cas.services.domain.DefaultDomainAwareServicesManager;
import org.apereo.cas.services.domain.DefaultRegisteredServiceDomainExtractor;
import org.apereo.cas.services.replication.NoOpRegisteredServiceReplicationStrategy;
import org.apereo.cas.services.replication.RegisteredServiceReplicationStrategy;
import org.apereo.cas.services.resource.DefaultRegisteredServiceResourceNamingStrategy;
import org.apereo.cas.services.resource.RegisteredServiceResourceNamingStrategy;
import org.apereo.cas.services.util.RegisteredServiceYamlHttpMessageConverter;
import org.apereo.cas.util.io.CommunicationsManager;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link CasCoreServicesConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreServicesConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@EnableAsync
public class CasCoreServicesConfiguration {
    @Autowired
    @Qualifier("communicationsManager")
    private ObjectProvider<CommunicationsManager> communicationsManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private Environment environment;

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "shibbolethCompatiblePersistentIdGenerator")
    public PersistentIdGenerator shibbolethCompatiblePersistentIdGenerator() {
        return new ShibbolethCompatiblePersistentIdGenerator();
    }

    @ConditionalOnMissingBean(name = "webApplicationResponseBuilderLocator")
    @Bean
    public ResponseBuilderLocator webApplicationResponseBuilderLocator() {
        val beans = applicationContext.getBeansOfType(ResponseBuilder.class, false, true);
        val builders = new ArrayList<ResponseBuilder>(beans.values());
        AnnotationAwareOrderComparator.sortIfNecessary(builders);
        return new DefaultWebApplicationResponseBuilderLocator(builders);
    }

    @Bean
    @ConditionalOnMissingBean(name = "webApplicationServiceResponseBuilder")
    public ResponseBuilder<WebApplicationService> webApplicationServiceResponseBuilder() {
        return new WebApplicationServiceResponseBuilder(servicesManager());
    }

    @ConditionalOnMissingBean(name = "registeredServiceCipherExecutor")
    @Bean
    @RefreshScope
    public RegisteredServiceCipherExecutor registeredServiceCipherExecutor() {
        return new RegisteredServicePublicKeyCipherExecutor();
    }

    @ConditionalOnMissingBean(name = "registeredServiceAccessStrategyEnforcer")
    @Bean
    @RefreshScope
    public AuditableExecution registeredServiceAccessStrategyEnforcer() {
        return new RegisteredServiceAccessStrategyAuditableEnforcer();
    }

    @ConditionalOnMissingBean(name = "servicesManager")
    @Bean
    @RefreshScope
    public ServicesManager servicesManager() {
        val configurers = applicationContext.getBeansOfType(ServicesManagerExecutionPlanConfigurer.class, false, true);
        val chain = new ChainingServicesManager();
        configurers.values().forEach(c -> chain.registerServiceManager(c.configureServicesManager()));
        return chain;
    }

    @Bean
    public HttpMessageConverter yamlHttpMessageConverter() {
        return new RegisteredServiceYamlHttpMessageConverter();
    }

    @Bean
    public RegisteredServicesEventListener registeredServicesEventListener() {
        return new RegisteredServicesEventListener(servicesManager(), casProperties, communicationsManager.getObject());
    }

    @ConditionalOnMissingBean(name = "registeredServiceReplicationStrategy")
    @Bean
    @RefreshScope
    public RegisteredServiceReplicationStrategy registeredServiceReplicationStrategy() {
        return new NoOpRegisteredServiceReplicationStrategy();
    }

    @ConditionalOnMissingBean(name = "registeredServiceResourceNamingStrategy")
    @Bean
    @RefreshScope
    public RegisteredServiceResourceNamingStrategy registeredServiceResourceNamingStrategy() {
        return new DefaultRegisteredServiceResourceNamingStrategy();
    }

    @Bean
    @Lazy(false)
    public ServiceRegistryExecutionPlan serviceRegistryExecutionPlan() {
        val configurers = applicationContext.getBeansOfType(ServiceRegistryExecutionPlanConfigurer.class, false, true);
        val plan = new DefaultServiceRegistryExecutionPlan();
        configurers.values().forEach(c -> {
            LOGGER.trace("Configuring service registry [{}]", c.getName());
            c.configureServiceRegistry(plan);
        });
        return plan;
    }

    @ConditionalOnProperty(prefix = "cas.service-registry.schedule", name = "enabled", havingValue = "true", matchIfMissing = true)
    @Bean
    public Runnable servicesManagerScheduledLoader() {
        val plan = serviceRegistryExecutionPlan();
        val filter = (Predicate) Predicates.not(Predicates.instanceOf(ImmutableServiceRegistry.class));
        if (!plan.find(filter).isEmpty()) {
            LOGGER.trace("Background task to load services is enabled to run every [{}]",
                casProperties.getServiceRegistry().getSchedule().getRepeatInterval());
            return new ServicesManagerScheduledLoader(servicesManager());
        }
        LOGGER.trace("Background task to load services is disabled");
        return ServicesManagerScheduledLoader.noOp();
    }

    @Bean
    @ConditionalOnMissingBean(name = "serviceRegistryListeners")
    public Collection<ServiceRegistryListener> serviceRegistryListeners() {
        return applicationContext.getBeansOfType(ServiceRegistryListener.class, false, true).values();
    }

    @ConditionalOnMissingBean(name = "serviceRegistry")
    @Bean
    @RefreshScope
    @Lazy(false)
    public ChainingServiceRegistry serviceRegistry() {
        val plan = serviceRegistryExecutionPlan();
        val filter = (Predicate) Predicates.not(Predicates.instanceOf(ImmutableServiceRegistry.class));

        val chainingRegistry = new DefaultChainingServiceRegistry(applicationContext);
        if (plan.find(filter).isEmpty()) {
            LOGGER.warn("Runtime memory is used as the persistence storage for retrieving and persisting service definitions. "
                + "Changes that are made to service definitions during runtime WILL be LOST when the CAS server is restarted. "
                + "Ideally for production, you should choose a storage option (JSON, JDBC, MongoDb, etc) to track service definitions.");
            chainingRegistry.addServiceRegistry(inMemoryServiceRegistry());
        }

        chainingRegistry.addServiceRegistries(plan.getServiceRegistries());
        return chainingRegistry;
    }

    @Bean
    @ConditionalOnMissingBean(name = "inMemoryServiceRegistry")
    public ServiceRegistry inMemoryServiceRegistry() {
        val services = getInMemoryRegisteredServices().orElseGet(ArrayList::new);
        return new InMemoryServiceRegistry(applicationContext, services, serviceRegistryListeners());
    }

    /**
     * Refresh CAS services after application has loaded.
     *
     * @param event the event
     */
    @EventListener
    @Async
    public void refreshServicesManagerWhenReady(final ApplicationReadyEvent event) {
        servicesManager().load();
    }

    @Bean
    @ConditionalOnMissingBean(name = "defaultServicesManagerExecutionPlanConfigurer")
    @ConditionalOnProperty(prefix = "cas.service-registry", name = "management-type", havingValue = "DEFAULT", matchIfMissing = true)
    public ServicesManagerExecutionPlanConfigurer defaultServicesManagerExecutionPlanConfigurer() {
        return () -> {
            val activeProfiles = Arrays.stream(environment.getActiveProfiles()).collect(Collectors.toSet());
            return new DefaultServicesManager(serviceRegistry(), applicationContext, activeProfiles);
        };
    }

    @Bean
    @ConditionalOnMissingBean(name = "domainServicesManagerExecutionPlanConfigurer")
    @ConditionalOnProperty(prefix = "cas.service-registry", name = "management-type", havingValue = "DOMAIN")
    public ServicesManagerExecutionPlanConfigurer domainServicesManagerExecutionPlanConfigurer() {
        return () -> {
            val activeProfiles = Arrays.stream(environment.getActiveProfiles()).collect(Collectors.toSet());
            return new DefaultDomainAwareServicesManager(serviceRegistry(), applicationContext,
                new DefaultRegisteredServiceDomainExtractor(),
                activeProfiles);
        };
    }

    private Optional<List<RegisteredService>> getInMemoryRegisteredServices() {
        if (applicationContext.containsBean("inMemoryRegisteredServices")) {
            return Optional.of(applicationContext.getBean("inMemoryRegisteredServices", List.class));
        }
        return Optional.empty();
    }
}
