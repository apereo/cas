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
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.services.ChainingServiceRegistry;
import org.apereo.cas.services.ChainingServicesManager;
import org.apereo.cas.services.DefaultChainingServiceRegistry;
import org.apereo.cas.services.DefaultServiceRegistryExecutionPlan;
import org.apereo.cas.services.DefaultServicesManager;
import org.apereo.cas.services.DefaultServicesManagerRegisteredServiceLocator;
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
import org.apereo.cas.services.ServicesManagerConfigurationContext;
import org.apereo.cas.services.ServicesManagerExecutionPlanConfigurer;
import org.apereo.cas.services.ServicesManagerRegisteredServiceLocator;
import org.apereo.cas.services.ServicesManagerScheduledLoader;
import org.apereo.cas.services.domain.DefaultDomainAwareServicesManager;
import org.apereo.cas.services.domain.DefaultRegisteredServiceDomainExtractor;
import org.apereo.cas.services.replication.NoOpRegisteredServiceReplicationStrategy;
import org.apereo.cas.services.replication.RegisteredServiceReplicationStrategy;
import org.apereo.cas.services.resource.DefaultRegisteredServiceResourceNamingStrategy;
import org.apereo.cas.services.resource.RegisteredServiceResourceNamingStrategy;
import org.apereo.cas.web.UrlValidator;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link CasCoreServicesConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "casCoreServicesConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@EnableAsync
public class CasCoreServicesConfiguration {

    private static Optional<List<RegisteredService>> getInMemoryRegisteredServices(final ApplicationContext applicationContext) {
        if (applicationContext.containsBean("inMemoryRegisteredServices")) {
            return Optional.of(applicationContext.getBean("inMemoryRegisteredServices", List.class));
        }
        return Optional.empty();
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "shibbolethCompatiblePersistentIdGenerator")
    public PersistentIdGenerator shibbolethCompatiblePersistentIdGenerator() {
        return new ShibbolethCompatiblePersistentIdGenerator();
    }

    @ConditionalOnMissingBean(name = "webApplicationResponseBuilderLocator")
    @Bean
    @Autowired
    public ResponseBuilderLocator webApplicationResponseBuilderLocator(
        final ConfigurableApplicationContext applicationContext) {
        val beans = applicationContext.getBeansOfType(ResponseBuilder.class, false, true);
        val builders = new ArrayList<>(beans.values());
        AnnotationAwareOrderComparator.sortIfNecessary(builders);
        return new DefaultWebApplicationResponseBuilderLocator(builders);
    }

    @ConditionalOnMissingBean(name = RegisteredServiceCipherExecutor.DEFAULT_BEAN_NAME)
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
    @ConditionalOnMissingBean(name = "serviceRegistryListeners")
    @RefreshScope
    @Autowired
    public List<ServiceRegistryListener> serviceRegistryListeners(final ConfigurableApplicationContext applicationContext) {
        return new ArrayList<>(applicationContext.getBeansOfType(ServiceRegistryListener.class, false, true).values());
    }

    @Bean
    @ConditionalOnMissingBean(name = "inMemoryServiceRegistry")
    @RefreshScope
    @Autowired
    public ServiceRegistry inMemoryServiceRegistry(
        @Qualifier("serviceRegistryListeners")
        final List<ServiceRegistryListener> serviceRegistryListeners,
        final ConfigurableApplicationContext applicationContext) {
        val services = getInMemoryRegisteredServices(applicationContext).orElseGet(ArrayList::new);
        return new InMemoryServiceRegistry(applicationContext, services, serviceRegistryListeners);
    }

    @Bean
    @Autowired
    @ConditionalOnMissingBean(name = "defaultServicesManagerExecutionPlanConfigurer")
    @ConditionalOnProperty(prefix = "cas.service-registry.core", name = "management-type", havingValue = "DEFAULT", matchIfMissing = true)
    public ServicesManagerExecutionPlanConfigurer defaultServicesManagerExecutionPlanConfigurer(
        @Qualifier("serviceRegistry")
        final ChainingServiceRegistry serviceRegistry,
        @Qualifier("servicesManagerCache")
        final Cache<Long, RegisteredService> servicesManagerCache,
        @Qualifier("servicesManagerRegisteredServiceLocators")
        final List<ServicesManagerRegisteredServiceLocator> servicesManagerRegisteredServiceLocators,
        final Environment environment,
        final ConfigurableApplicationContext applicationContext) {
        return () -> {
            val activeProfiles = Arrays.stream(environment.getActiveProfiles()).collect(Collectors.toSet());
            val context = ServicesManagerConfigurationContext.builder()
                .serviceRegistry(serviceRegistry)
                .applicationContext(applicationContext)
                .environments(activeProfiles)
                .servicesCache(servicesManagerCache)
                .registeredServiceLocators(servicesManagerRegisteredServiceLocators)
                .build();
            return new DefaultServicesManager(context);
        };
    }

    @Configuration(value = "CasCoreServicesExecutionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreServicesExecutionConfiguration {

        @ConditionalOnProperty(prefix = "cas.service-registry.schedule", name = "enabled", havingValue = "true", matchIfMissing = true)
        @Bean
        @Autowired
        public Runnable servicesManagerScheduledLoader(
            final CasConfigurationProperties casProperties,
            @Qualifier("serviceRegistryExecutionPlan")
            final ServiceRegistryExecutionPlan serviceRegistryExecutionPlan,
            @Qualifier("servicesManager")
            final ServicesManager servicesManager) {
            val filter = (Predicate) Predicates.not(Predicates.instanceOf(ImmutableServiceRegistry.class));
            if (!serviceRegistryExecutionPlan.find(filter).isEmpty()) {
                LOGGER.trace("Background task to load services is enabled to run every [{}]",
                    casProperties.getServiceRegistry().getSchedule().getRepeatInterval());
                return new ServicesManagerScheduledLoader(servicesManager);
            }
            LOGGER.trace("Background task to load services is disabled");
            return ServicesManagerScheduledLoader.noOp();
        }

        @ConditionalOnMissingBean(name = "serviceRegistryExecutionPlan")
        @Bean
        @RefreshScope
        @Autowired
        public ServiceRegistryExecutionPlan serviceRegistryExecutionPlan(
            final ConfigurableApplicationContext applicationContext) {
            val configurers = applicationContext.getBeansOfType(ServiceRegistryExecutionPlanConfigurer.class, false, true);
            val plan = new DefaultServiceRegistryExecutionPlan();
            configurers.values().forEach(Unchecked.consumer(c -> {
                LOGGER.trace("Configuring service registry [{}]", c.getName());
                c.configureServiceRegistry(plan);
            }));
            return plan;
        }

        @ConditionalOnMissingBean(name = "serviceRegistry")
        @Bean
        @RefreshScope
        @Autowired
        public ChainingServiceRegistry serviceRegistry(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("serviceRegistryExecutionPlan")
            final ServiceRegistryExecutionPlan serviceRegistryExecutionPlan,
            @Qualifier("inMemoryServiceRegistry")
            final ServiceRegistry inMemoryServiceRegistry) {
            val filter = (Predicate) Predicates.not(Predicates.instanceOf(ImmutableServiceRegistry.class));

            val chainingRegistry = new DefaultChainingServiceRegistry(applicationContext);
            if (serviceRegistryExecutionPlan.find(filter).isEmpty()) {
                LOGGER.warn("Runtime memory is used as the persistence storage for retrieving and persisting service definitions. "
                    + "Changes that are made to service definitions during runtime WILL be LOST when the CAS server is restarted. "
                    + "Ideally for production, you should choose a storage option (JSON, JDBC, MongoDb, etc) to track service definitions.");
                chainingRegistry.addServiceRegistry(inMemoryServiceRegistry);
            }
            chainingRegistry.addServiceRegistries(serviceRegistryExecutionPlan.getServiceRegistries());
            return chainingRegistry;
        }
    }

    @Configuration(value = "CasCoreServicesManagerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreServicesManagerConfiguration {
        @Bean
        @RefreshScope
        @ConditionalOnMissingBean(name = "servicesManagerRegisteredServiceLocators")
        @Autowired
        public List<ServicesManagerRegisteredServiceLocator> servicesManagerRegisteredServiceLocators(
            final ConfigurableApplicationContext applicationContext) {
            val locators = applicationContext.getBeansOfType(ServicesManagerRegisteredServiceLocator.class, false, true);
            val sortedLocators = new ArrayList<>(locators.values());
            AnnotationAwareOrderComparator.sortIfNecessary(sortedLocators);
            return sortedLocators;
        }

        @Bean
        @ConditionalOnMissingBean(name = "defaultServicesManagerRegisteredServiceLocator")
        public ServicesManagerRegisteredServiceLocator defaultServicesManagerRegisteredServiceLocator() {
            return new DefaultServicesManagerRegisteredServiceLocator();
        }

        @Bean
        @Autowired
        @ConditionalOnMissingBean(name = "domainServicesManagerExecutionPlanConfigurer")
        @ConditionalOnProperty(prefix = "cas.service-registry.core", name = "management-type", havingValue = "DOMAIN")
        public ServicesManagerExecutionPlanConfigurer domainServicesManagerExecutionPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("serviceRegistry")
            final ChainingServiceRegistry serviceRegistry,
            @Qualifier("servicesManagerCache")
            final Cache<Long, RegisteredService> servicesManagerCache,
            final Environment environment) {
            return () -> {
                val activeProfiles = Arrays.stream(environment.getActiveProfiles()).collect(Collectors.toSet());
                val context = ServicesManagerConfigurationContext.builder()
                    .serviceRegistry(serviceRegistry)
                    .applicationContext(applicationContext)
                    .environments(activeProfiles)
                    .servicesCache(servicesManagerCache)
                    .registeredServiceLocators(List.of(new DefaultServicesManagerRegisteredServiceLocator()))
                    .build();
                return new DefaultDomainAwareServicesManager(context, new DefaultRegisteredServiceDomainExtractor());
            };
        }

        @ConditionalOnMissingBean(name = "servicesManager")
        @Bean
        @RefreshScope
        @Autowired
        public ServicesManager servicesManager(final ConfigurableApplicationContext applicationContext) {
            val configurers = applicationContext.getBeansOfType(ServicesManagerExecutionPlanConfigurer.class, false, true);
            val chain = new ChainingServicesManager();
            configurers.values().forEach(c -> chain.registerServiceManager(c.configureServicesManager()));
            return chain;
        }

        @Bean
        @Autowired
        public RegisteredServicesEventListener registeredServicesEventListener(
            final CasConfigurationProperties casProperties,
            @Qualifier("servicesManager")
            final ServicesManager servicesManager,
            @Qualifier("communicationsManager")
            final CommunicationsManager communicationsManager) {
            return new RegisteredServicesEventListener(servicesManager, casProperties, communicationsManager);
        }

        @Bean
        @ConditionalOnMissingBean(name = "webApplicationServiceResponseBuilder")
        @Autowired
        public ResponseBuilder<WebApplicationService> webApplicationServiceResponseBuilder(
            @Qualifier("servicesManager")
            final ServicesManager servicesManager,
            @Qualifier("urlValidator")
            final UrlValidator urlValidator) {
            return new WebApplicationServiceResponseBuilder(servicesManager, urlValidator);
        }

        @RefreshScope
        @Bean
        @ConditionalOnMissingBean(name = "servicesManagerCache")
        @Autowired
        public Cache<Long, RegisteredService> servicesManagerCache(final CasConfigurationProperties casProperties) {
            val cacheProperties = casProperties.getServiceRegistry().getCache();
            val builder = Caffeine.newBuilder();
            val duration = Beans.newDuration(cacheProperties.getDuration());
            return builder
                .initialCapacity(cacheProperties.getInitialCapacity())
                .maximumSize(cacheProperties.getCacheSize())
                .expireAfterWrite(duration)
                .build();
        }

        @EventListener
        @Async
        public void refreshServicesManagerWhenReady(final ApplicationReadyEvent event) {
            val servicesManager = event.getApplicationContext().getBean("servicesManager", ServicesManager.class);
            servicesManager.load();
        }
    }
}
