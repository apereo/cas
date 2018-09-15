package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.couchdb.services.RegisteredServiceCouchDbRepository;
import org.apereo.cas.services.CouchDbServiceRegistry;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlan;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;

import lombok.val;
import org.ektorp.impl.ObjectMapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CouchDbServiceRegistryConfiguration}.
 *
 * @author Timur Duehr
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-support-couchdb-service-registry")
@Configuration("couchDbServiceRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CouchDbServiceRegistryConfiguration implements ServiceRegistryExecutionPlanConfigurer {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("serviceRegistryCouchDbFactory")
    private CouchDbConnectorFactory couchDbFactory;

    @Autowired
    @Qualifier("defaultObjectMapperFactory")
    private ObjectMapperFactory objectMapperFactory;

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "serviceRegistryCouchDbFactory")
    public CouchDbConnectorFactory serviceRegistryCouchDbFactory() {
        return new CouchDbConnectorFactory(casProperties.getServiceRegistry().getCouchDb(), objectMapperFactory);
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "serviceRegistryCouchDbRepository")
    public RegisteredServiceCouchDbRepository serviceRegistryCouchDbRepository() {
        val couchDbProperties = casProperties.getServiceRegistry().getCouchDb();

        val serviceRepository = new RegisteredServiceCouchDbRepository(couchDbFactory.getCouchDbConnector(), couchDbProperties.isCreateIfNotExists());
        serviceRepository.initStandardDesignDocument();
        return serviceRepository;
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "couchDbServiceRegistry")
    public ServiceRegistry couchDbServiceRegistry() {
        return new CouchDbServiceRegistry(serviceRegistryCouchDbRepository(), casProperties.getServiceRegistry().getCouchDb().getRetries());
    }

    @Override
    public void configureServiceRegistry(final ServiceRegistryExecutionPlan plan) {
        plan.registerServiceRegistry(couchDbServiceRegistry());
    }
}
