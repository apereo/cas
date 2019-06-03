package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.services.MongoDbServiceRegistry;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlan;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServiceRegistryListener;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Collection;

/**
 * This is {@link MongoDbServiceRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("mongoDbServiceRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MongoDbServiceRegistryConfiguration {
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("serviceRegistryListeners")
    private ObjectProvider<Collection<ServiceRegistryListener>> serviceRegistryListeners;

    @ConditionalOnMissingBean(name = "mongoDbServiceRegistryTemplate")
    @Bean
    public MongoTemplate mongoDbServiceRegistryTemplate() {
        val mongo = casProperties.getServiceRegistry().getMongo();
        val factory = new MongoDbConnectionFactory();

        val mongoTemplate = factory.buildMongoTemplate(mongo);
        factory.createCollection(mongoTemplate, mongo.getCollection(), mongo.isDropCollection());
        return mongoTemplate;
    }

    @Bean
    public ServiceRegistry mongoDbServiceRegistry() {
        val mongo = casProperties.getServiceRegistry().getMongo();
        return new MongoDbServiceRegistry(
            eventPublisher,
            mongoDbServiceRegistryTemplate(),
            mongo.getCollection(),
            serviceRegistryListeners.getIfAvailable());
    }

    @Bean
    @ConditionalOnMissingBean(name = "mongoDbServiceRegistryExecutionPlanConfigurer")
    public ServiceRegistryExecutionPlanConfigurer mongoDbServiceRegistryExecutionPlanConfigurer() {
        return new ServiceRegistryExecutionPlanConfigurer() {
            @Override
            public void configureServiceRegistry(final ServiceRegistryExecutionPlan plan) {
                plan.registerServiceRegistry(mongoDbServiceRegistry());
            }
        };
    }
}
