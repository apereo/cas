package org.apereo.cas.config;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.DefaultWebApplicationResponseBuilderLocator;
import org.apereo.cas.authentication.principal.ResponseBuilder;
import org.apereo.cas.authentication.principal.ResponseBuilderLocator;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.authentication.principal.WebApplicationServiceResponseBuilder;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.services.ChainingServiceRegistry;
import org.apereo.cas.services.ChainingServicesManager;
import org.apereo.cas.services.DefaultChainingServiceRegistry;
import org.apereo.cas.services.DefaultRegisteredServicePrincipalAccessStrategyEnforcer;
import org.apereo.cas.services.DefaultRegisteredServicesEventListener;
import org.apereo.cas.services.DefaultRegisteredServicesTemplatesManager;
import org.apereo.cas.services.DefaultServiceRegistryExecutionPlan;
import org.apereo.cas.services.DefaultServicesManagerRegisteredServiceLocator;
import org.apereo.cas.services.GroovyRegisteredServiceAccessStrategyEnforcer;
import org.apereo.cas.services.ImmutableServiceRegistry;
import org.apereo.cas.services.InMemoryServiceRegistry;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyAuditableEnforcer;
import org.apereo.cas.services.RegisteredServiceAccessStrategyEnforcer;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.services.RegisteredServiceIndexService;
import org.apereo.cas.services.RegisteredServicePrincipalAccessStrategyEnforcer;
import org.apereo.cas.services.RegisteredServicePublicKeyCipherExecutor;
import org.apereo.cas.services.RegisteredServicesEventListener;
import org.apereo.cas.services.RegisteredServicesTemplatesManager;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlan;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServiceRegistryListener;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.ServicesManagerConfigurationContext;
import org.apereo.cas.services.ServicesManagerExecutionPlanConfigurer;
import org.apereo.cas.services.ServicesManagerRegisteredServiceLocator;
import org.apereo.cas.services.ServicesManagerScheduledLoader;
import org.apereo.cas.services.mgmt.DefaultChainingServicesManager;
import org.apereo.cas.services.mgmt.DefaultServicesManager;
import org.apereo.cas.services.query.DefaultRegisteredServiceIndexService;
import org.apereo.cas.services.replication.NoOpRegisteredServiceReplicationStrategy;
import org.apereo.cas.services.replication.RegisteredServiceReplicationStrategy;
import org.apereo.cas.services.resource.DefaultRegisteredServiceResourceNamingStrategy;
import org.apereo.cas.services.resource.RegisteredServiceResourceNamingStrategy;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import org.apereo.cas.util.spring.CasApplicationReadyListener;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.util.spring.boot.ConditionalOnMissingGraalVMNativeImage;
import org.apereo.cas.web.UrlValidator;
import com.github.benmanes.caffeine.cache.Cache;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.ScopedProxyMode;
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
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@EnableAsync(proxyTargetClass = false)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.ServiceRegistry)
@Configuration(value = "CasCoreServicesConfiguration", proxyBeanMethods = false)
class CasCoreServicesConfiguration {
    @Configuration(value = "CasCoreServicesResponseLocatorConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreServicesResponseLocatorConfiguration {
        @ConditionalOnMissingBean(name = "webApplicationResponseBuilderLocator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ResponseBuilderLocator webApplicationResponseBuilderLocator(final List<ResponseBuilder> responseBuilders) {
            AnnotationAwareOrderComparator.sortIfNecessary(responseBuilders);
            return new DefaultWebApplicationResponseBuilderLocator(responseBuilders);
        }

    }

    @Configuration(value = "CasCoreServicesEventsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
    static class CasCoreServicesEventsConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Lazy(false)
        public RegisteredServicesEventListener registeredServicesEventListener(
            final CasConfigurationProperties casProperties,
            @Qualifier(TenantExtractor.BEAN_NAME)
            final TenantExtractor tenantExtractor,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(CommunicationsManager.BEAN_NAME)
            final CommunicationsManager communicationsManager) {
            return new DefaultRegisteredServicesEventListener(servicesManager,
                casProperties, communicationsManager, tenantExtractor);
        }
    }

    @Configuration(value = "CasCoreServicesResponseBuilderConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreServicesResponseBuilderConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "webApplicationServiceResponseBuilder")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ResponseBuilder<WebApplicationService> webApplicationServiceResponseBuilder(
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(UrlValidator.BEAN_NAME)
            final UrlValidator urlValidator) {
            return new WebApplicationServiceResponseBuilder(servicesManager, urlValidator);
        }
    }

    @Configuration(value = "CasCoreServicesBaseConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreServicesBaseConfiguration {
        @ConditionalOnMissingBean(name = RegisteredServiceCipherExecutor.DEFAULT_BEAN_NAME)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public RegisteredServiceCipherExecutor registeredServiceCipherExecutor() {
            return new RegisteredServicePublicKeyCipherExecutor();
        }

        @ConditionalOnMissingBean(name = AuditableExecution.AUDITABLE_EXECUTION_REGISTERED_SERVICE_ACCESS)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditableExecution registeredServiceAccessStrategyEnforcer(
            @Qualifier(RegisteredServicePrincipalAccessStrategyEnforcer.BEAN_NAME)
            final RegisteredServicePrincipalAccessStrategyEnforcer principalAccessStrategyEnforcer,
            final ConfigurableApplicationContext applicationContext) {
            return new RegisteredServiceAccessStrategyAuditableEnforcer(applicationContext, principalAccessStrategyEnforcer);
        }

        @ConditionalOnMissingBean(name = RegisteredServicePrincipalAccessStrategyEnforcer.BEAN_NAME)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public RegisteredServicePrincipalAccessStrategyEnforcer principalAccessStrategyEnforcer(final ConfigurableApplicationContext applicationContext) {
            return new DefaultRegisteredServicePrincipalAccessStrategyEnforcer(applicationContext);
        }
        
        @ConditionalOnMissingBean(name = "groovyRegisteredServiceAccessStrategyEnforcer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingGraalVMNativeImage
        public RegisteredServiceAccessStrategyEnforcer groovyRegisteredServiceAccessStrategyEnforcer(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(RegisteredServiceAccessStrategyEnforcer.class)
                .when(BeanCondition.on("cas.access-strategy.groovy.location").exists()
                    .given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val location = casProperties.getAccessStrategy().getGroovy().getLocation();
                    val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
                    return new GroovyRegisteredServiceAccessStrategyEnforcer(scriptFactory.fromResource(location));
                })
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "CasCoreServicesStrategyConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreServicesStrategyConfiguration {

        @ConditionalOnMissingBean(name = "registeredServiceReplicationStrategy")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public RegisteredServiceReplicationStrategy registeredServiceReplicationStrategy() {
            return new NoOpRegisteredServiceReplicationStrategy();
        }

        @ConditionalOnMissingBean(name = RegisteredServiceResourceNamingStrategy.BEAN_NAME)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public RegisteredServiceResourceNamingStrategy registeredServiceResourceNamingStrategy() {
            return new DefaultRegisteredServiceResourceNamingStrategy();
        }
    }

    @Configuration(value = "CasCoreServiceRegistryPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreServiceRegistryPlanConfiguration {
        @ConditionalOnMissingBean(name = "serviceRegistryExecutionPlan")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ServiceRegistryExecutionPlan serviceRegistryExecutionPlan(
            final ObjectProvider<List<ServiceRegistryExecutionPlanConfigurer>> provider) {
            val plan = new DefaultServiceRegistryExecutionPlan();
            provider.ifAvailable(configurers -> configurers
                .stream()
                .filter(BeanSupplier::isNotProxy)
                .forEach(Unchecked.consumer(c -> {
                    LOGGER.trace("Configuring service registry [{}]", c.getName());
                    c.configureServiceRegistry(plan);
                })));
            return plan;
        }
    }

    @Configuration(value = "CasCoreServicesListenerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreServicesListenerConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "defaultServiceRegistryListener")
        public ServiceRegistryListener defaultServiceRegistryListener() {
            return ServiceRegistryListener.noOp();
        }

    }

    @Configuration(value = "CasCoreServiceRegistryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreServiceRegistryConfiguration {

        private static Optional<List<RegisteredService>> getInMemoryRegisteredServices(final ApplicationContext applicationContext) {
            if (applicationContext.containsBean("inMemoryRegisteredServices")) {
                return Optional.of(applicationContext.getBean("inMemoryRegisteredServices", List.class));
            }
            return Optional.empty();
        }

        @ConditionalOnMissingBean(name = ServiceRegistry.BEAN_NAME)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ChainingServiceRegistry serviceRegistry(
            final ConfigurableApplicationContext applicationContext,
            final ObjectProvider<List<ServiceRegistryListener>> serviceRegistryListeners,
            @Qualifier("serviceRegistryExecutionPlan")
            final ServiceRegistryExecutionPlan serviceRegistryExecutionPlan) {
            val filter = (Predicate) Predicates.not(Predicates.instanceOf(ImmutableServiceRegistry.class));
            val chainingRegistry = new DefaultChainingServiceRegistry(applicationContext);
            if (serviceRegistryExecutionPlan.find(filter).isEmpty()) {
                LOGGER.info("Runtime memory is used as the persistence storage for retrieving and persisting service definitions. "
                            + "Changes that are made to service definitions during runtime WILL be LOST when the CAS server is restarted. "
                            + "Ideally for production, you should choose a storage option (JSON, JDBC, MongoDb, etc) to track service definitions.");
                val services = getInMemoryRegisteredServices(applicationContext).orElseGet(ArrayList::new);
                val inMemoryServiceRegistry = new InMemoryServiceRegistry(applicationContext, services,
                    Optional.ofNullable(serviceRegistryListeners.getIfAvailable()).orElseGet(ArrayList::new));
                chainingRegistry.addServiceRegistry(inMemoryServiceRegistry);
            }
            val serviceRegistries = serviceRegistryExecutionPlan.getServiceRegistries();
            AnnotationAwareOrderComparator.sort(serviceRegistries);

            chainingRegistry.addServiceRegistries(serviceRegistries);
            return chainingRegistry;
        }
    }

    @Configuration(value = "CasCoreServicesManagerExecutionPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreServicesManagerExecutionPlanConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = ServicesManagerConfigurationContext.BEAN_NAME)
        public ServicesManagerConfigurationContext servicesManagerConfigurationContext(
            @Qualifier(TenantExtractor.BEAN_NAME)
            final TenantExtractor tenantExtractor,
            final CasConfigurationProperties casProperties,
            @Qualifier(RegisteredServicesTemplatesManager.BEAN_NAME)
            final RegisteredServicesTemplatesManager registeredServicesTemplatesManager,
            @Qualifier(ServiceRegistry.BEAN_NAME)
            final ChainingServiceRegistry serviceRegistry,
            @Qualifier(RegisteredServiceIndexService.BEAN_NAME)
            final RegisteredServiceIndexService registeredServiceIndexService,
            @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
            final ServiceFactory serviceFactory,
            @Qualifier("servicesManagerCache")
            final Cache<Long, RegisteredService> servicesManagerCache,
            final List<ServicesManagerRegisteredServiceLocator> servicesManagerRegisteredServiceLocators,
            final Environment environment,
            final ConfigurableApplicationContext applicationContext) {

            AnnotationAwareOrderComparator.sortIfNecessary(servicesManagerRegisteredServiceLocators);
            val activeProfiles = Arrays.stream(environment.getActiveProfiles()).collect(Collectors.toSet());
            return ServicesManagerConfigurationContext
                .builder()
                .serviceRegistry(serviceRegistry)
                .applicationContext(applicationContext)
                .environments(activeProfiles)
                .servicesCache(servicesManagerCache)
                .registeredServicesTemplatesManager(registeredServicesTemplatesManager)
                .registeredServiceLocators(servicesManagerRegisteredServiceLocators)
                .casProperties(casProperties)
                .tenantExtractor(tenantExtractor)
                .serviceFactory(serviceFactory)
                .registeredServiceIndexService(registeredServiceIndexService)
                .build();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "defaultServicesManagerExecutionPlanConfigurer")
        public ServicesManagerExecutionPlanConfigurer defaultServicesManagerExecutionPlanConfigurer(
            final CasConfigurationProperties casProperties,
            @Qualifier(ServicesManagerConfigurationContext.BEAN_NAME)
            final ServicesManagerConfigurationContext configurationContext) {
            return () -> new DefaultServicesManager(configurationContext);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "defaultServicesManagerRegisteredServiceLocator")
        public ServicesManagerRegisteredServiceLocator defaultServicesManagerRegisteredServiceLocator() {
            return new DefaultServicesManagerRegisteredServiceLocator();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = RegisteredServiceIndexService.BEAN_NAME)
        public RegisteredServiceIndexService registeredServiceIndexService(
            final List<ServicesManagerRegisteredServiceLocator> servicesManagerRegisteredServiceLocators,
            final CasConfigurationProperties casProperties) {
            return new DefaultRegisteredServiceIndexService(servicesManagerRegisteredServiceLocators, casProperties);
        }
        
    }

    @Configuration(value = "CasCoreServicesManagerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @AutoConfigureAfter(CasCoreServiceRegistryConfiguration.class)
    static class CasCoreServicesManagerConfiguration {

        @ConditionalOnMissingBean(name = RegisteredServicesTemplatesManager.BEAN_NAME)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public RegisteredServicesTemplatesManager registeredServicesTemplatesManager(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(RegisteredServicesTemplatesManager.class)
                .when(BeanCondition.on("cas.service-registry.templates.directory.location").exists().given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val registeredServiceSerializer = new RegisteredServiceJsonSerializer(applicationContext);
                    return new DefaultRegisteredServicesTemplatesManager(casProperties.getServiceRegistry(), registeredServiceSerializer);
                })
                .otherwise(RegisteredServicesTemplatesManager::noOp)
                .get();
            
        }

        @ConditionalOnMissingBean(name = ServicesManager.BEAN_NAME)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ChainingServicesManager servicesManager(final List<ServicesManagerExecutionPlanConfigurer> configurers) {
            val chain = new DefaultChainingServicesManager();
            AnnotationAwareOrderComparator.sortIfNecessary(configurers);
            configurers.forEach(cfg -> chain.registerServiceManager(cfg.configureServicesManager()));
            return chain;
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "servicesManagerCache")
        public Cache<Long, RegisteredService> servicesManagerCache(final CasConfigurationProperties casProperties) {
            return Beans.newCacheBuilder(casProperties.getServiceRegistry().getCache()).build();
        }

        @Bean
        @Lazy(false)
        public CasApplicationReadyListener servicesManagerApplicationReady(
            @Qualifier(ServicesManager.BEAN_NAME) final ChainingServicesManager servicesManager,
            final CasConfigurationProperties casProperties) {
            return event -> servicesManager.load();
        }
    }

    @Configuration(value = "CasCoreServicesSchedulingConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
    @AutoConfigureAfter({CasCoreServicesManagerConfiguration.class, CasCoreServiceRegistryConfiguration.class})
    static class CasCoreServicesSchedulingConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Lazy(false)
        public Runnable servicesManagerScheduledLoader(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("serviceRegistryExecutionPlan")
            final ServiceRegistryExecutionPlan serviceRegistryExecutionPlan,
            final CasConfigurationProperties casProperties,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {

            return BeanSupplier.of(Runnable.class)
                .when(BeanCondition.on("cas.service-registry.schedule.enabled").isTrue().evenIfMissing()
                    .given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val filter = (Predicate) Predicates.not(Predicates.instanceOf(ImmutableServiceRegistry.class));
                    if (!serviceRegistryExecutionPlan.find(filter).isEmpty()) {
                        LOGGER.trace("Background task to load services is enabled to run every [{}]",
                            casProperties.getServiceRegistry().getSchedule().getRepeatInterval());
                        return new ServicesManagerScheduledLoader(servicesManager);
                    }
                    LOGGER.trace("Background task to load services is disabled");
                    return ServicesManagerScheduledLoader.noOp();
                })
                .otherwiseProxy()
                .get();
        }
    }
}
