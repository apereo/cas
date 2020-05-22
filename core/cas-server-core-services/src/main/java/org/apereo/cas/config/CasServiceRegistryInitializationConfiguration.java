package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ChainingServiceRegistry;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServiceRegistryInitializer;
import org.apereo.cas.services.ServiceRegistryInitializerEventListener;
import org.apereo.cas.services.ServiceRegistryListener;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.resource.AbstractResourceBasedServiceRegistry;
import org.apereo.cas.services.util.CasAddonsRegisteredServicesJsonSerializer;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.io.WatcherService;
import org.apereo.cas.util.serialization.StringSerializer;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

/**
 * This is {@link CasServiceRegistryInitializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("casServiceRegistryInitializationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnMissingClass(value = {
    "org.apereo.cas.services.JsonServiceRegistry",
    "org.apereo.cas.services.YamlServiceRegistry"
})
@ConditionalOnBean(ServicesManager.class)
@ConditionalOnProperty(prefix = "cas.service-registry", name = "init-from-json", havingValue = "true")
@Slf4j
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableAsync
public class CasServiceRegistryInitializationConfiguration {

    @Autowired
    @Qualifier("serviceRegistryListeners")
    private ObjectProvider<Collection<ServiceRegistryListener>> serviceRegistryListeners;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("serviceRegistry")
    private ObjectProvider<ChainingServiceRegistry> serviceRegistry;

    @Lazy(false)
    @Bean
    public ServiceRegistryInitializer serviceRegistryInitializer() {
        val serviceRegistryInstance = serviceRegistry.getObject();
        val initializer = new ServiceRegistryInitializer(embeddedJsonServiceRegistry(),
            serviceRegistryInstance, servicesManager.getObject());
        LOGGER.info("Attempting to initialize the service registry [{}]", serviceRegistryInstance.getName());
        initializer.initServiceRegistryIfNecessary();
        return initializer;
    }

    @Bean
    public ServiceRegistryInitializerEventListener serviceRegistryInitializerConfigurationEventListener() {
        return new ServiceRegistryInitializerEventListener(serviceRegistryInitializer());
    }

    @RefreshScope
    @Bean
    @SneakyThrows
    @Lazy(false)
    public ServiceRegistry embeddedJsonServiceRegistry() {
        val location = getServiceRegistryInitializerServicesDirectoryResource();
        val registry = new EmbeddedResourceBasedServiceRegistry(applicationContext, location,
            serviceRegistryListeners.getObject(), WatcherService.noOp());
        if (!(location instanceof ClassPathResource)) {
            registry.enableDefaultWatcherService();
        }
        return registry;
    }

    @Bean
    @ConditionalOnMissingBean(name = "embeddedJsonServiceRegistryExecutionPlanConfigurer")
    public ServiceRegistryExecutionPlanConfigurer embeddedJsonServiceRegistryExecutionPlanConfigurer() {
        return plan -> plan.registerServiceRegistry(embeddedJsonServiceRegistry());
    }

    @SneakyThrows
    private Resource getServiceRegistryInitializerServicesDirectoryResource() {
        val registry = casProperties.getServiceRegistry().getJson();
        if (ResourceUtils.doesResourceExist(registry.getLocation())) {
            LOGGER.debug("Using JSON service registry location [{}] for embedded service definitions", registry.getLocation());
            return registry.getLocation();
        }
        val parent = new File(FileUtils.getTempDirectory(), "cas");
        if (!parent.mkdirs()) {
            LOGGER.warn("Unable to create folder [{}]", parent);
        }
        val resources = ResourcePatternUtils.getResourcePatternResolver(applicationContext)
            .getResources("classpath*:/services/*.json");
        Arrays.stream(resources)
            .forEach(resource -> ResourceUtils.exportClasspathResourceToFile(parent, resource));
        LOGGER.debug("Using JSON service registry location [{}] for embedded service definitions", registry.getLocation());
        return new FileSystemResource(parent);
    }

    /**
     * The embedded service registry that processes built-in JSON service files
     * on the classpath.
     */
    public static class EmbeddedResourceBasedServiceRegistry extends AbstractResourceBasedServiceRegistry {
        EmbeddedResourceBasedServiceRegistry(final ConfigurableApplicationContext applicationContext,
                                             final Resource location,
                                             final Collection<ServiceRegistryListener> serviceRegistryListeners,
                                             final WatcherService watcherService) throws Exception {
            super(location, getRegisteredServiceSerializers(), applicationContext, serviceRegistryListeners, watcherService);
        }

        static Collection<StringSerializer<RegisteredService>> getRegisteredServiceSerializers() {
            return CollectionUtils.wrapList(
                new CasAddonsRegisteredServicesJsonSerializer(),
                new RegisteredServiceJsonSerializer());
        }

        @Override
        protected String[] getExtensions() {
            return new String[]{"json"};
        }
    }
}
