package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.couchdb.services.RegisteredServiceCouchDbRepository;
import org.apereo.cas.services.CouchDbServiceRegistry;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServiceRegistryListener;

import lombok.val;
import org.ektorp.impl.ObjectMapperFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link CouchDbServiceRegistryConfiguration}.
 *
 * @author Timur Duehr
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-support-couchdb-service-registry")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "couchDbServiceRegistryConfiguration", proxyBeanMethods = false)
public class CouchDbServiceRegistryConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "serviceRegistryCouchDbFactory")
    @Autowired
    public CouchDbConnectorFactory serviceRegistryCouchDbFactory(
        final CasConfigurationProperties casProperties,
        @Qualifier("defaultObjectMapperFactory")
        final ObjectMapperFactory objectMapperFactory) {
        return new CouchDbConnectorFactory(casProperties.getServiceRegistry().getCouchDb(), objectMapperFactory);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "serviceRegistryCouchDbRepository")
    @Autowired
    public RegisteredServiceCouchDbRepository serviceRegistryCouchDbRepository(
        final CasConfigurationProperties casProperties,
        @Qualifier("serviceRegistryCouchDbFactory")
        final CouchDbConnectorFactory couchDbFactory) {
        val couchDbProperties = casProperties.getServiceRegistry().getCouchDb();
        val serviceRepository = new RegisteredServiceCouchDbRepository(couchDbFactory.getCouchDbConnector(),
            couchDbProperties.isCreateIfNotExists());
        serviceRepository.initStandardDesignDocument();
        return serviceRepository;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "couchDbServiceRegistry")
    @Autowired
    public ServiceRegistry couchDbServiceRegistry(
        final ConfigurableApplicationContext applicationContext,
        final ObjectProvider<List<ServiceRegistryListener>> serviceRegistryListeners,
        @Qualifier("serviceRegistryCouchDbRepository")
        final RegisteredServiceCouchDbRepository serviceRegistryCouchDbRepository) {
        return new CouchDbServiceRegistry(applicationContext, serviceRegistryCouchDbRepository,
            Optional.ofNullable(serviceRegistryListeners.getIfAvailable()).orElseGet(ArrayList::new));
    }

    @Configuration(value = "CouchDbServiceRegistryPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CouchDbServiceRegistryPlanConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "couchDbServiceRegistryExecutionPlanConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ServiceRegistryExecutionPlanConfigurer couchDbServiceRegistryExecutionPlanConfigurer(
            @Qualifier("couchDbServiceRegistry")
            final ServiceRegistry couchDbServiceRegistry) {
            return plan -> plan.registerServiceRegistry(couchDbServiceRegistry);
        }
    }

}
