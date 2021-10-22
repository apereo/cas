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
import org.apereo.cas.services.DefaultChainingServicesManager;
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
import org.apereo.cas.util.spring.CasEventListener;
import org.apereo.cas.web.UrlValidator;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.env.Environment;
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
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
public class CasCoreServicesConfiguration {
    @Configuration(value = "CasCoreServicesResponseLocatorConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreServicesResponseLocatorConfiguration {
        @ConditionalOnMissingBean(name = "webApplicationResponseBuilderLocator")
        @Bean
        @Autowired
        public ResponseBuilderLocator webApplicationResponseBuilderLocator(final List<ResponseBuilder> responseBuilders) {
            AnnotationAwareOrderComparator.sortIfNecessary(responseBuilders);
            return new DefaultWebApplicationResponseBuilderLocator(responseBuilders);
        }

    }

    @Configuration(value = "CasCoreServicesEventsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
    public static class CasCoreServicesEventsConfiguration {

        @Bean
        @Autowired
        public CasEventListener registeredServicesEventListener(
            final CasConfigurationProperties casProperties,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier("communicationsManager")
            final CommunicationsManager communicationsManager) {
            return new RegisteredServicesEventListener(servicesManager, casProperties, communicationsManager);
        }
    }

    @Configuration(value = "CasCoreServicesResponseBuilderConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreServicesResponseBuilderConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "webApplicationServiceResponseBuilder")
        @Autowired
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ResponseBuilder<WebApplicationService> webApplicationServiceResponseBuilder(
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier("urlValidator")
            final UrlValidator urlValidator) {
            return new WebApplicationServiceResponseBuilder(servicesManager, urlValidator);
        }
    }

    @Configuration(value = "CasCoreServicesBaseConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreServicesBaseConfiguration {

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "shibbolethCompatiblePersistentIdGenerator")
        public PersistentIdGenerator shibbolethCompatiblePersistentIdGenerator() {
            return new ShibbolethCompatiblePersistentIdGenerator();
        }

        @ConditionalOnMissingBean(name = RegisteredServiceCipherExecutor.DEFAULT_BEAN_NAME)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public RegisteredServiceCipherExecutor registeredServiceCipherExecutor() {
            return new RegisteredServicePublicKeyCipherExecutor();
        }

        @ConditionalOnMissingBean(name = "registeredServiceAccessStrategyEnforcer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditableExecution registeredServiceAccessStrategyEnforcer(final CasConfigurationProperties casProperties) {
            return new RegisteredServiceAccessStrategyAuditableEnforcer(casProperties);
        }

    }

    @Configuration(value = "CasCoreServicesStrategyConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreServicesStrategyConfiguration {

        @ConditionalOnMissingBean(name = "registeredServiceReplicationStrategy")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public RegisteredServiceReplicationStrategy registeredServiceReplicationStrategy() {
            return new NoOpRegisteredServiceReplicationStrategy();
        }

        @ConditionalOnMissingBean(name = "registeredServiceResourceNamingStrategy")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public RegisteredServiceResourceNamingStrategy registeredServiceResourceNamingStrategy() {
            return new DefaultRegisteredServiceResourceNamingStrategy();
        }
    }

    @Configuration(value = "CasCoreServiceRegistryPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreServiceRegistryPlanConfiguration {
        @ConditionalOnMissingBean(name = "serviceRegistryExecutionPlan")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public ServiceRegistryExecutionPlan serviceRegistryExecutionPlan(
            final ObjectProvider<List<ServiceRegistryExecutionPlanConfigurer>> provider) {
            val plan = new DefaultServiceRegistryExecutionPlan();
            provider.ifAvailable(configurers -> {
                configurers.forEach(Unchecked.consumer(c -> {
                    LOGGER.trace("Configuring service registry [{}]", c.getName());
                    c.configureServiceRegistry(plan);
                }));
            });
            return plan;
        }
    }

    @Configuration(value = "CasCoreServicesListenerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreServicesListenerConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "defaultServiceRegistryListener")
        public ServiceRegistryListener defaultServiceRegistryListener() {
            return ServiceRegistryListener.noOp();
        }

    }

    @Configuration(value = "CasCoreServiceRegistryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreServiceRegistryConfiguration {

        private static Optional<List<RegisteredService>> getInMemoryRegisteredServices(final ApplicationContext applicationContext) {
            if (applicationContext.containsBean("inMemoryRegisteredServices")) {
                return Optional.of(applicationContext.getBean("inMemoryRegisteredServices", List.class));
            }
            return Optional.empty();
        }

        @ConditionalOnMissingBean(name = "serviceRegistry")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public ChainingServiceRegistry serviceRegistry(
            final ConfigurableApplicationContext applicationContext,
            final ObjectProvider<List<ServiceRegistryListener>> serviceRegistryListeners,
            @Qualifier("serviceRegistryExecutionPlan")
            final ServiceRegistryExecutionPlan serviceRegistryExecutionPlan) throws Exception {
            val filter = (Predicate) Predicates.not(Predicates.instanceOf(ImmutableServiceRegistry.class));
            val chainingRegistry = new DefaultChainingServiceRegistry(applicationContext);
            if (serviceRegistryExecutionPlan.find(filter).isEmpty()) {
                LOGGER.warn("Runtime memory is used as the persistence storage for retrieving and persisting service definitions. "
                            + "Changes that are made to service definitions during runtime WILL be LOST when the CAS server is restarted. "
                            + "Ideally for production, you should choose a storage option (JSON, JDBC, MongoDb, etc) to track service definitions.");
                val services = getInMemoryRegisteredServices(applicationContext).orElseGet(ArrayList::new);
                val inMemoryServiceRegistry = new InMemoryServiceRegistry(applicationContext, services,
                    Optional.ofNullable(serviceRegistryListeners.getIfAvailable()).orElseGet(ArrayList::new));
                chainingRegistry.addServiceRegistry(inMemoryServiceRegistry);
            }
            chainingRegistry.addServiceRegistries(serviceRegistryExecutionPlan.getServiceRegistries());
            return chainingRegistry;
        }
    }

    @Configuration(value = "CasCoreServicesManagerExecutionPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreServicesManagerExecutionPlanConfiguration {
        @Bean
        public ServicesManagerConfigurationContext servicesManagerConfigurationContext(
            @Qualifier("serviceRegistry")
            final ChainingServiceRegistry serviceRegistry,
            @Qualifier("servicesManagerCache")
            final Cache<Long, RegisteredService> servicesManagerCache,
            final List<ServicesManagerRegisteredServiceLocator> servicesManagerRegisteredServiceLocators,
            final Environment environment,
            final ConfigurableApplicationContext applicationContext) {
            AnnotationAwareOrderComparator.sortIfNecessary(servicesManagerRegisteredServiceLocators);
            val activeProfiles = Arrays.stream(environment.getActiveProfiles()).collect(Collectors.toSet());
            return ServicesManagerConfigurationContext.builder()
                .serviceRegistry(serviceRegistry)
                .applicationContext(applicationContext)
                .environments(activeProfiles)
                .servicesCache(servicesManagerCache)
                .registeredServiceLocators(servicesManagerRegisteredServiceLocators)
                .build();
        }

        @Bean
        @Autowired
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "defaultServicesManagerExecutionPlanConfigurer")
        @ConditionalOnProperty(prefix = "cas.service-registry.core", name = "management-type", havingValue = "DEFAULT", matchIfMissing = true)
        public ServicesManagerExecutionPlanConfigurer defaultServicesManagerExecutionPlanConfigurer(
            @Qualifier("servicesManagerConfigurationContext")
            final ServicesManagerConfigurationContext servicesManagerConfigurationContext) {
            return () -> new DefaultServicesManager(servicesManagerConfigurationContext);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "defaultServicesManagerRegisteredServiceLocator")
        public ServicesManagerRegisteredServiceLocator defaultServicesManagerRegisteredServiceLocator() {
            return new DefaultServicesManagerRegisteredServiceLocator();
        }

        @Bean
        @Autowired
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "domainServicesManagerExecutionPlanConfigurer")
        @ConditionalOnProperty(prefix = "cas.service-registry.core", name = "management-type", havingValue = "DOMAIN")
        public ServicesManagerExecutionPlanConfigurer domainServicesManagerExecutionPlanConfigurer(
            @Qualifier("servicesManagerConfigurationContext")
            final ServicesManagerConfigurationContext servicesManagerConfigurationContext) {
            return () -> new DefaultDomainAwareServicesManager(servicesManagerConfigurationContext, new DefaultRegisteredServiceDomainExtractor());
        }
    }

    @Configuration(value = "CasCoreServicesManagerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @AutoConfigureAfter(CasCoreServiceRegistryConfiguration.class)
    public static class CasCoreServicesManagerConfiguration {
        @ConditionalOnMissingBean(name = ServicesManager.BEAN_NAME)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ChainingServicesManager servicesManager(final List<ServicesManagerExecutionPlanConfigurer> configurers) {
            val chain = new DefaultChainingServicesManager();
            AnnotationAwareOrderComparator.sortIfNecessary(configurers);
            configurers.forEach(c -> chain.registerServiceManager(c.configureServicesManager()));
            return chain;
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
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
        public void refreshServicesManagerWhenReady(final ApplicationReadyEvent event) {
            val servicesManager = event.getApplicationContext().getBean(ServicesManager.BEAN_NAME, ChainingServicesManager.class);
            servicesManager.load();
        }
    }

    @Configuration(value = "CasCoreServicesSchedulingConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
    @AutoConfigureAfter({CasCoreServicesManagerConfiguration.class, CasCoreServiceRegistryConfiguration.class})
    @ConditionalOnProperty(prefix = "cas.service-registry.schedule", name = "enabled", havingValue = "true", matchIfMissing = true)
    public static class CasCoreServicesSchedulingConfiguration {
        @Bean
        @Autowired
        public Runnable servicesManagerScheduledLoader(
            @Qualifier("serviceRegistryExecutionPlan")
            final ServiceRegistryExecutionPlan serviceRegistryExecutionPlan,
            final CasConfigurationProperties casProperties,
            @Qualifier(ServicesManager.BEAN_NAME)
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
    }
}
