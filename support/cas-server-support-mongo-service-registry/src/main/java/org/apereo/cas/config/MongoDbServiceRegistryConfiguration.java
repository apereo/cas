package org.apereo.cas.config;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.services.MongoDbServiceRegistry;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServiceRegistryListener;

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
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.TextIndexDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link MongoDbServiceRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "mongoDbServiceRegistryConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MongoDbServiceRegistryConfiguration {

    @ConditionalOnMissingBean(name = "mongoDbServiceRegistryTemplate")
    @Bean
    @Autowired
    public MongoTemplate mongoDbServiceRegistryTemplate(
        final CasConfigurationProperties casProperties,
        @Qualifier("casSslContext")
        final CasSSLContext casSslContext) {
        val mongo = casProperties.getServiceRegistry().getMongo();
        val factory = new MongoDbConnectionFactory(casSslContext.getSslContext());
        val mongoTemplate = factory.buildMongoTemplate(mongo);
        MongoDbConnectionFactory.createCollection(mongoTemplate, mongo.getCollection(), mongo.isDropCollection());
        val collection = mongoTemplate.getCollection(mongo.getCollection());
        val columnsIndex = new TextIndexDefinition.TextIndexDefinitionBuilder().onField("id").onField("serviceId").onField("name").build();
        MongoDbConnectionFactory.createOrUpdateIndexes(mongoTemplate, collection, List.of(columnsIndex));
        return mongoTemplate;
    }

    @Bean
    @Autowired
    @ConditionalOnMissingBean(name = "mongoDbServiceRegistry")
    public ServiceRegistry mongoDbServiceRegistry(
        @Qualifier("mongoDbServiceRegistryTemplate")
        final MongoTemplate mongoDbServiceRegistryTemplate,
        final ObjectProvider<List<ServiceRegistryListener>> serviceRegistryListeners,
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext) {
        val mongo = casProperties.getServiceRegistry().getMongo();
        return new MongoDbServiceRegistry(applicationContext, mongoDbServiceRegistryTemplate, mongo.getCollection(),
            Optional.ofNullable(serviceRegistryListeners.getIfAvailable()).orElseGet(ArrayList::new));
    }

    @Bean
    @ConditionalOnMissingBean(name = "mongoDbServiceRegistryExecutionPlanConfigurer")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public ServiceRegistryExecutionPlanConfigurer mongoDbServiceRegistryExecutionPlanConfigurer(
        @Qualifier("mongoDbServiceRegistry")
        final ServiceRegistry mongoDbServiceRegistry) {
        return plan -> plan.registerServiceRegistry(mongoDbServiceRegistry);
    }
}
