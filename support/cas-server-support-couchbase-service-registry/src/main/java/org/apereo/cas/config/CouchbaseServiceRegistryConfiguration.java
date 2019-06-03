package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.couchbase.core.CouchbaseClientFactory;
import org.apereo.cas.services.CouchbaseServiceRegistry;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlan;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServiceRegistryListener;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

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
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("serviceRegistryListeners")
    private ObjectProvider<Collection<ServiceRegistryListener>> serviceRegistryListeners;

    @RefreshScope
    @Bean
    public CouchbaseClientFactory serviceRegistryCouchbaseClientFactory() {
        val couchbase = casProperties.getServiceRegistry().getCouchbase();
        val nodes = StringUtils.commaDelimitedListToSet(couchbase.getNodeSet());
        return new CouchbaseClientFactory(nodes, couchbase.getBucket(),
            couchbase.getPassword(),
            Beans.newDuration(couchbase.getTimeout()).toMillis());
    }

    @Bean
    @RefreshScope
    public ServiceRegistry couchbaseServiceRegistry() {
        return new CouchbaseServiceRegistry(eventPublisher, serviceRegistryCouchbaseClientFactory(),
            new RegisteredServiceJsonSerializer(), serviceRegistryListeners.getIfAvailable());
    }

    @Bean
    @ConditionalOnMissingBean(name = "couchbaseServiceRegistryExecutionPlanConfigurer")
    public ServiceRegistryExecutionPlanConfigurer couchbaseServiceRegistryExecutionPlanConfigurer() {
        return new ServiceRegistryExecutionPlanConfigurer() {
            @Override
            public void configureServiceRegistry(final ServiceRegistryExecutionPlan plan) {
                plan.registerServiceRegistry(couchbaseServiceRegistry());
            }
        };
    }
}
