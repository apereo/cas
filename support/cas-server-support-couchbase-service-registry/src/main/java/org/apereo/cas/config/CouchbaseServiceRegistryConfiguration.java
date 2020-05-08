package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.couchbase.core.CouchbaseClientFactory;
import org.apereo.cas.services.CouchbaseServiceRegistry;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServiceRegistryListener;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;

import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;

/**
 * This is {@link CouchbaseServiceRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("couchbaseServiceRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CouchbaseServiceRegistryConfiguration {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("serviceRegistryListeners")
    private ObjectProvider<Collection<ServiceRegistryListener>> serviceRegistryListeners;

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "serviceRegistryCouchbaseClientFactory")
    public CouchbaseClientFactory serviceRegistryCouchbaseClientFactory() {
        val couchbase = casProperties.getServiceRegistry().getCouchbase();
        return new CouchbaseClientFactory(couchbase);
    }

    @Bean
    @RefreshScope
    public ServiceRegistry couchbaseServiceRegistry() {
        return new CouchbaseServiceRegistry(applicationContext, serviceRegistryCouchbaseClientFactory(),
            new RegisteredServiceJsonSerializer(new MinimalPrettyPrinter()),
            serviceRegistryListeners.getObject());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "couchbaseServiceRegistryExecutionPlanConfigurer")
    public ServiceRegistryExecutionPlanConfigurer couchbaseServiceRegistryExecutionPlanConfigurer() {
        return plan -> plan.registerServiceRegistry(couchbaseServiceRegistry());
    }
}
