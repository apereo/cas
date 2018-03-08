package org.apereo.cas.config;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mongo.serviceregistry.MongoDbServiceRegistryProperties;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.services.MongoServiceRegistry;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlan;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * This is {@link MongoDbServiceRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("mongoDbServiceRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class MongoDbServiceRegistryConfiguration implements ServiceRegistryExecutionPlanConfigurer {

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "mongoDbServiceRegistryTemplate")
    @Bean
    public MongoTemplate mongoDbServiceRegistryTemplate() {
        final MongoDbServiceRegistryProperties mongo = casProperties.getServiceRegistry().getMongo();
        final MongoDbConnectionFactory factory = new MongoDbConnectionFactory();

        final MongoTemplate mongoTemplate = factory.buildMongoTemplate(mongo);
        factory.createCollection(mongoTemplate, mongo.getCollection(), mongo.isDropCollection());
        return mongoTemplate;
    }
    
    @Bean
    public ServiceRegistry mongoDbServiceRegistry() {
        final MongoDbServiceRegistryProperties mongo = casProperties.getServiceRegistry().getMongo();
        return new MongoServiceRegistry(
                mongoDbServiceRegistryTemplate(),
                mongo.getCollection());
    }

    @Override
    public void configureServiceRegistry(final ServiceRegistryExecutionPlan plan) {
        plan.registerServiceRegistry(mongoDbServiceRegistry());
    }
}
