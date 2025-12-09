package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.ChainingServiceRegistry;
import org.apereo.cas.services.DefaultServiceRegistryInitializer;
import org.apereo.cas.services.DefaultServiceRegistryInitializerEventListener;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServiceRegistryInitializer;
import org.apereo.cas.services.ServiceRegistryInitializerEventListener;
import org.apereo.cas.services.ServiceRegistryListener;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.resource.AbstractResourceBasedServiceRegistry;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.io.WatcherService;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jooq.lambda.Unchecked;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.EnableAsync;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link CasServiceRegistryInitializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnMissingClass({
    "org.apereo.cas.services.JsonServiceRegistry",
    "org.apereo.cas.services.YamlServiceRegistry"
})
@ConditionalOnBean(ServicesManager.class)
@Slf4j
@EnableAspectJAutoProxy(proxyTargetClass = false)
@EnableAsync(proxyTargetClass = false)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.ServiceRegistry)
@Configuration(value = "CasServiceRegistryInitializationConfiguration", proxyBeanMethods = false)
class CasServiceRegistryInitializationConfiguration {

    private static final BeanCondition CONDITION = BeanCondition.on("cas.service-registry.core.init-from-json").isTrue();

    @Configuration(value = "CasServiceRegistryInitializationEventsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasServiceRegistryInitializationEventsConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Lazy(false)
        public ServiceRegistryInitializerEventListener serviceRegistryInitializerConfigurationEventListener(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("serviceRegistryInitializer")
            final ObjectProvider<@NonNull ServiceRegistryInitializer> serviceRegistryInitializer) {
            return BeanSupplier.of(ServiceRegistryInitializerEventListener.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new DefaultServiceRegistryInitializerEventListener(serviceRegistryInitializer))
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "CasServiceRegistryInitializationBaseConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasServiceRegistryInitializationBaseConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Lazy(false)
        public ServiceRegistryInitializer serviceRegistryInitializer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("embeddedJsonServiceRegistry")
            final ServiceRegistry embeddedJsonServiceRegistry,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(ServiceRegistry.BEAN_NAME)
            final ChainingServiceRegistry serviceRegistry) {
            return BeanSupplier.of(ServiceRegistryInitializer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new DefaultServiceRegistryInitializer(embeddedJsonServiceRegistry, serviceRegistry, servicesManager))
                .otherwiseProxy()
                .get();
        }

    }

    @Configuration(value = "CasServiceRegistryEmbeddedConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasServiceRegistryEmbeddedConfiguration {
        private static Resource getServiceRegistryInitializerServicesDirectoryResource(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            val registry = casProperties.getServiceRegistry().getJson();
            if (ResourceUtils.doesResourceExist(registry.getLocation())
                || (ResourceUtils.isJarResource(registry.getLocation()) && !registry.isUsingDefaultLocation())) {
                LOGGER.debug("Using JSON service registry location [{}] for embedded service definitions", registry.getLocation());
                return registry.getLocation();
            }
            val parent = new File(FileUtils.getTempDirectory(), "cas");
            if (!parent.mkdirs() && !parent.exists()) {
                LOGGER.warn("Unable to create folder [{}]", parent);
            }
            val baseName = FilenameUtils.getBaseName(registry.getLocation().getFilename());
            val patterns = Arrays.stream(applicationContext.getEnvironment().getActiveProfiles())
                .map(profile -> String.format("classpath*:/%s/%s/*.json", baseName, profile))
                .collect(Collectors.toList());

            if (casProperties.getServiceRegistry().getCore().isInitDefaultServices()) {
                patterns.add("classpath*:/services/*.json");
            }
            LOGGER.debug("Patterns to scan for embedded service definitions: [{}]", patterns);
            ResourceUtils.exportResources(applicationContext, parent, patterns);
            LOGGER.debug("Using service registry location [{}] for embedded service definitions", parent);
            return new FileSystemResource(parent);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public ServiceRegistry embeddedJsonServiceRegistry(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            final ObjectProvider<@NonNull List<ServiceRegistryListener>> serviceRegistryListeners) {
            return BeanSupplier.of(ServiceRegistry.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(Unchecked.supplier(() -> {
                    val location = getServiceRegistryInitializerServicesDirectoryResource(casProperties, applicationContext);
                    val registry = new EmbeddedResourceBasedServiceRegistry(applicationContext, location,
                        Optional.ofNullable(serviceRegistryListeners.getIfAvailable()).orElseGet(ArrayList::new), WatcherService.noOp());
                    if (!(location instanceof ClassPathResource) && casProperties.getServiceRegistry().getJson().isWatcherEnabled()) {
                        registry.enableDefaultWatcherService();
                    }
                    return registry;
                }))
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "embeddedJsonServiceRegistryExecutionPlanConfigurer")
        public ServiceRegistryExecutionPlanConfigurer embeddedJsonServiceRegistryExecutionPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("embeddedJsonServiceRegistry")
            final ServiceRegistry embeddedJsonServiceRegistry) {
            return BeanSupplier.of(ServiceRegistryExecutionPlanConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> plan -> plan.registerServiceRegistry(embeddedJsonServiceRegistry))
                .otherwiseProxy()
                .get();
        }
    }

    /**
     * The embedded service registry that processes built-in JSON service files
     * on the classpath.
     */
    static class EmbeddedResourceBasedServiceRegistry extends AbstractResourceBasedServiceRegistry {
        EmbeddedResourceBasedServiceRegistry(final ConfigurableApplicationContext applicationContext,
                                             final Resource location,
                                             final Collection<ServiceRegistryListener> serviceRegistryListeners,
                                             final WatcherService watcherService) {
            super(location, CollectionUtils.wrapList(new RegisteredServiceJsonSerializer(applicationContext)),
                applicationContext, serviceRegistryListeners, watcherService);
        }

        @Override
        protected String[] getExtensions() {
            return new String[]{"json"};
        }
    }
}
