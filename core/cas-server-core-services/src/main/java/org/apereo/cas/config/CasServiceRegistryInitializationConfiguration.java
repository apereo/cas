package org.apereo.cas.config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.services.ServiceRegistryProperties;
import org.apereo.cas.configuration.model.support.services.json.JsonServiceRegistryProperties;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryInitializer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.resource.AbstractResourceBasedServiceRegistry;
import org.apereo.cas.services.util.CasAddonsRegisteredServicesJsonSerializer;
import org.apereo.cas.services.util.DefaultRegisteredServiceJsonSerializer;
import org.apereo.cas.util.CollectionUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
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
    "org.apereo.cas.services.YamlServiceRegistry"})
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

    @RefreshScope
    @Bean
    public ServiceRegistryInitializer serviceRegistryInitializer() {
        final ServiceRegistryProperties props = casProperties.getServiceRegistry();
        final ServiceRegistry serviceRegistryInstance = serviceRegistry.getIfAvailable();
        final ServiceRegistryInitializer initializer =
            new ServiceRegistryInitializer(embeddedJsonServiceRegistry(), serviceRegistryInstance,
                servicesManager.getIfAvailable(), props.isInitFromJson());

        if (props.isInitFromJson()) {
            LOGGER.info("Attempting to initialize the service registry [{}] from service definition resources found at [{}]",
                serviceRegistryInstance.getName(),
                getServiceRegistryInitializerServicesDirectoryResource());
        }
        initializer.initServiceRegistryIfNecessary();
        return initializer;
    }

    @RefreshScope
    @Bean
    @SneakyThrows
    public ServiceRegistry embeddedJsonServiceRegistry() {
        final Resource location = getServiceRegistryInitializerServicesDirectoryResource();
        return new EmbeddedServiceRegistry(eventPublisher, location);
    }

    private Resource getServiceRegistryInitializerServicesDirectoryResource() {
        final JsonServiceRegistryProperties registry = casProperties.getServiceRegistry().getJson();
        return ObjectUtils.defaultIfNull(registry.getLocation(), new ClassPathResource("services"));
    }

    /**
     * The embedded service registry that processes built-in JSON service files
     * on the classpath.
     */
    public static class EmbeddedServiceRegistry extends AbstractResourceBasedServiceRegistry {
        EmbeddedServiceRegistry(final ApplicationEventPublisher publisher, final Resource location) throws Exception {
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
