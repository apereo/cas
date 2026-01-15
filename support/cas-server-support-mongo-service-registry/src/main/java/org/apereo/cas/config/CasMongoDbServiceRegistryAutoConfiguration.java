package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.services.MongoDbServiceRegistry;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServiceRegistryListener;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.TextIndexDefinition;

/**
 * This is {@link CasMongoDbServiceRegistryAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.ServiceRegistry, module = "mongo")
@AutoConfiguration
public class CasMongoDbServiceRegistryAutoConfiguration {

    @ConditionalOnMissingBean(name = "mongoDbServiceRegistryTemplate")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MongoTemplate mongoDbServiceRegistryTemplate(
        final CasConfigurationProperties casProperties,
        @Qualifier(CasSSLContext.BEAN_NAME)
        final CasSSLContext casSslContext) {
        val mongo = casProperties.getServiceRegistry().getMongo();
        val factory = new MongoDbConnectionFactory(casSslContext.getSslContext());
        val mongoTemplate = factory.buildMongoTemplate(mongo);
        MongoDbConnectionFactory.createCollection(mongoTemplate, mongo.getCollection(), mongo.isDropCollection());
        val collection = mongoTemplate.getCollection(mongo.getCollection());
        val columnsIndex = new TextIndexDefinition.TextIndexDefinitionBuilder().onField("id")
            .onField("serviceId").onField("name").build();
        MongoDbConnectionFactory.createOrUpdateIndexes(mongoTemplate, collection, List.of(columnsIndex));
        return mongoTemplate.asMongoTemplate();
    }

    @Bean
    @ConditionalOnMissingBean(name = "mongoDbServiceRegistry")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public ServiceRegistry mongoDbServiceRegistry(
        @Qualifier("mongoDbServiceRegistryTemplate")
        final MongoOperations mongoDbServiceRegistryTemplate,
        final ObjectProvider<@NonNull List<ServiceRegistryListener>> serviceRegistryListeners,
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext) {
        val mongo = casProperties.getServiceRegistry().getMongo();
        return new MongoDbServiceRegistry(applicationContext, mongoDbServiceRegistryTemplate, mongo.getCollection(),
            Optional.ofNullable(serviceRegistryListeners.getIfAvailable()).orElseGet(ArrayList::new));
    }

    @Bean
    @ConditionalOnMissingBean(name = "mongoDbServiceRegistryExecutionPlanConfigurer")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public ServiceRegistryExecutionPlanConfigurer mongoDbServiceRegistryExecutionPlanConfigurer(
        @Qualifier("mongoDbServiceRegistry")
        final ServiceRegistry mongoDbServiceRegistry) {
        return plan -> plan.registerServiceRegistry(mongoDbServiceRegistry);
    }
}
