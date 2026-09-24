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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

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
        return factory.buildMongoTemplate(mongo).asMongoTemplate();
    }

    @Bean
    @ConditionalOnMissingBean(name = "mongoDbServiceRegistryInitializer")
    public InitializingBean mongoDbServiceRegistryInitializer(
        @Qualifier("mongoDbServiceRegistryTemplate")
        final MongoOperations mongoDbServiceRegistryTemplate,
        final CasConfigurationProperties casProperties) {
        return () -> {
            val mongo = casProperties.getServiceRegistry().getMongo();
            MongoDbConnectionFactory.createCollection(mongoDbServiceRegistryTemplate, mongo.getCollection(), mongo.isDropCollection());
            val collection = mongoDbServiceRegistryTemplate.getCollection(mongo.getCollection());
            val serviceIdIndex = new Index().named("IDX_SERVICE_ID").on("serviceId", Sort.Direction.ASC);
            val serviceNameIndex = new Index().named("IDX_SERVICE_NAME").on("name", Sort.Direction.ASC);
            MongoDbConnectionFactory.createOrUpdateIndexes(mongoDbServiceRegistryTemplate, collection,
                List.of(serviceIdIndex, serviceNameIndex));
        };
    }

    @Bean
    @ConditionalOnMissingBean(name = "mongoDbServiceRegistry")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @DependsOn("mongoDbServiceRegistryInitializer")
    public ServiceRegistry mongoDbServiceRegistry(
        @Qualifier("mongoDbServiceRegistryTemplate")
        final MongoOperations mongoDbServiceRegistryTemplate,
        final ObjectProvider<List<ServiceRegistryListener>> serviceRegistryListeners,
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext) {
        val mongo = casProperties.getServiceRegistry().getMongo();
        val registry = new MongoDbServiceRegistry(applicationContext,
            mongoDbServiceRegistryTemplate,
            mongo.getCollection(),
            Optional.ofNullable(serviceRegistryListeners.getIfAvailable()).orElseGet(ArrayList::new));
        registry.setOrder(mongo.getOrder());
        return registry;
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
