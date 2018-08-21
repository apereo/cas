package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.CasServiceRegistryInitializerConfigurationEventListener;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryInitializer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.resource.AbstractResourceBasedServiceRegistry;
import org.apereo.cas.services.util.CasAddonsRegisteredServicesJsonSerializer;
import org.apereo.cas.services.util.DefaultRegisteredServiceJsonSerializer;
import org.apereo.cas.util.CollectionUtils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.List;

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
@ConditionalOnProperty(prefix = "cas.serviceRegistry", name = "initFromJson", havingValue = "true")
@Slf4j
public class CasServiceRegistryInitializationConfiguration {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("serviceRegistry")
    private ObjectProvider<ServiceRegistry> serviceRegistry;

    @Bean
    public ServiceRegistryInitializer serviceRegistryInitializer() {
        val serviceRegistryInstance = serviceRegistry.getIfAvailable();
        val initializer = new ServiceRegistryInitializer(embeddedJsonServiceRegistry(),
            serviceRegistryInstance, servicesManager.getIfAvailable());

        LOGGER.info("Attempting to initialize the service registry [{}] from service definition resources found at [{}]",
            serviceRegistryInstance.getName(),
            getServiceRegistryInitializerServicesDirectoryResource());
        initializer.initServiceRegistryIfNecessary();
        return initializer;
    }

    @Bean
    public CasServiceRegistryInitializerConfigurationEventListener serviceRegistryInitializerConfigurationEventListener() {
        return new CasServiceRegistryInitializerConfigurationEventListener(serviceRegistryInitializer());
    }

    @RefreshScope
    @Bean
    @SneakyThrows
    public ServiceRegistry embeddedJsonServiceRegistry() {
        val location = getServiceRegistryInitializerServicesDirectoryResource();
        return new EmbeddedResourceBasedServiceRegistry(eventPublisher, location);
    }

    private Resource getServiceRegistryInitializerServicesDirectoryResource() {
        val registry = casProperties.getServiceRegistry().getJson();
        return ObjectUtils.defaultIfNull(registry.getLocation(), new ClassPathResource("services"));
    }

    /**
     * The embedded service registry that processes built-in JSON service files
     * on the classpath.
     */
    public static class EmbeddedResourceBasedServiceRegistry extends AbstractResourceBasedServiceRegistry {
        EmbeddedResourceBasedServiceRegistry(final ApplicationEventPublisher publisher, final Resource location) throws Exception {
            super(location, getRegisteredServiceSerializers(), publisher);
        }

        private static List getRegisteredServiceSerializers() {
            return CollectionUtils.wrapList(
                new CasAddonsRegisteredServicesJsonSerializer(),
                new DefaultRegisteredServiceJsonSerializer());
        }

        @Override
        protected String getExtension() {
            return "json";
        }
    }
}
