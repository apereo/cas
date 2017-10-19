package org.apereo.cas.config;

import org.apache.commons.lang3.ObjectUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.services.ServiceRegistryProperties;
import org.apereo.cas.configuration.model.support.services.json.JsonServiceRegistryProperties;
import org.apereo.cas.services.AbstractResourceBasedServiceRegistryDao;
import org.apereo.cas.services.ServiceRegistryDao;
import org.apereo.cas.services.ServiceRegistryInitializer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.util.CasAddonsRegisteredServicesJsonSerializer;
import org.apereo.cas.services.util.DefaultRegisteredServiceJsonSerializer;
import org.apereo.cas.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/**
 * This is {@link CasServiceRegistryInitializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("casServiceRegistryInitializationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnMissingClass(value = {
        "org.apereo.cas.services.JsonServiceRegistryDao",
        "org.apereo.cas.services.YamlServiceRegistryDao"})
public class CasServiceRegistryInitializationConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasServiceRegistryInitializationConfiguration.class);

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @Autowired
    @Bean
    public ServiceRegistryInitializer serviceRegistryInitializer(@Qualifier("servicesManager") final ServicesManager servicesManager,
                                                                 @Qualifier("serviceRegistryDao") final ServiceRegistryDao serviceRegistryDao) {
        final ServiceRegistryProperties serviceRegistry = casProperties.getServiceRegistry();
        final ServiceRegistryInitializer initializer =
                new ServiceRegistryInitializer(embeddedJsonServiceRegistry(), serviceRegistryDao, servicesManager, serviceRegistry.isInitFromJson());

        if (serviceRegistry.isInitFromJson()) {
            LOGGER.info("Attempting to initialize the service registry [{}] from service definition resources found at [{}]",
                    serviceRegistryDao.toString(),
                    getServiceRegistryInitializerServicesDirectoryResource());
        }
        initializer.initServiceRegistryIfNecessary();
        return initializer;
    }

    @RefreshScope
    @Bean
    public ServiceRegistryDao embeddedJsonServiceRegistry() {
        try {
            final Resource location = getServiceRegistryInitializerServicesDirectoryResource();
            return new EmbeddedServiceRegistryDao(eventPublisher, location);
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private Resource getServiceRegistryInitializerServicesDirectoryResource() {
        final JsonServiceRegistryProperties registry = casProperties.getServiceRegistry().getJson();
        return ObjectUtils.defaultIfNull(registry.getLocation(), new ClassPathResource("services"));
    }

    /**
     * The embedded service registry that processes built-in JSON service files
     * on the classpath.
     */
    public static class EmbeddedServiceRegistryDao extends AbstractResourceBasedServiceRegistryDao {
        EmbeddedServiceRegistryDao(final ApplicationEventPublisher publisher, final Resource location) throws Exception {
            super(location, CollectionUtils.wrapList(
                    new CasAddonsRegisteredServicesJsonSerializer(),
                    new DefaultRegisteredServiceJsonSerializer()),
                    false, publisher);
        }

        @Override
        protected String getExtension() {
            return "json";
        }
    }

}
