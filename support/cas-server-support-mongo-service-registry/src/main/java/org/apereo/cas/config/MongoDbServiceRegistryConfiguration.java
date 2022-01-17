package org.apereo.cas.config;

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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.TextIndexDefinition;

import javax.net.ssl.SSLContext;
import java.util.Collection;
import java.util.List;

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
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("serviceRegistryListeners")
    private ObjectProvider<Collection<ServiceRegistryListener>> serviceRegistryListeners;

    @Autowired
    @Qualifier("sslContext")
    private ObjectProvider<SSLContext> sslContext;

    private MongoTemplate mongoDbServiceRegistryTemplate() {
        val mongo = casProperties.getServiceRegistry().getMongo();
        val factory = new MongoDbConnectionFactory(sslContext.getObject());

        val mongoTemplate = factory.buildMongoTemplate(mongo);
        factory.createCollection(mongoTemplate, mongo.getCollection(), mongo.isDropCollection());

        val collection = mongoTemplate.getCollection(mongo.getCollection());
        val columnsIndex = new TextIndexDefinition.TextIndexDefinitionBuilder()
            .onField("id")
            .onField("serviceId")
            .onField("name")
            .build();
        MongoDbConnectionFactory.createOrUpdateIndexes(mongoTemplate, collection, List.of(columnsIndex));

        return mongoTemplate;
    }

    @Bean
    @ConditionalOnMissingBean(name = "mongoDbServiceRegistry")
    public ServiceRegistry mongoDbServiceRegistry() {
        val mongo = casProperties.getServiceRegistry().getMongo();
        return new MongoDbServiceRegistry(
            applicationContext,
            mongoDbServiceRegistryTemplate(),
            mongo.getCollection(),
            serviceRegistryListeners.getObject());
    }

    @Bean
    @ConditionalOnMissingBean(name = "mongoDbServiceRegistryExecutionPlanConfigurer")
    @RefreshScope
    public ServiceRegistryExecutionPlanConfigurer mongoDbServiceRegistryExecutionPlanConfigurer() {
        return plan -> plan.registerServiceRegistry(mongoDbServiceRegistry());
    }
}
