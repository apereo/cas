package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.dynamodb.AmazonDynamoDbClientFactory;
import org.apereo.cas.services.DynamoDbServiceRegistry;
import org.apereo.cas.services.DynamoDbServiceRegistryFacilitator;
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
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * This is {@link CasDynamoDbServiceRegistryAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.ServiceRegistry, module = "dynamodb")
@AutoConfiguration
public class CasDynamoDbServiceRegistryAutoConfiguration {

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public DynamoDbServiceRegistryFacilitator dynamoDbServiceRegistryFacilitator(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier("amazonDynamoDbServiceRegistryClient")
        final DynamoDbClient amazonDynamoDbServiceRegistryClient) {
        val db = casProperties.getServiceRegistry().getDynamoDb();
        return new DynamoDbServiceRegistryFacilitator(db, amazonDynamoDbServiceRegistryClient, applicationContext);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "dynamoDbServiceRegistry")
    public ServiceRegistry dynamoDbServiceRegistry(
        final ConfigurableApplicationContext applicationContext,
        final ObjectProvider<@NonNull List<ServiceRegistryListener>> serviceRegistryListeners,
        @Qualifier("dynamoDbServiceRegistryFacilitator")
        final DynamoDbServiceRegistryFacilitator dynamoDbServiceRegistryFacilitator) {
        return new DynamoDbServiceRegistry(applicationContext, dynamoDbServiceRegistryFacilitator,
            Optional.ofNullable(serviceRegistryListeners.getIfAvailable()).orElseGet(ArrayList::new));
    }

    @Bean
    @ConditionalOnMissingBean(name = "dynamoDbServiceRegistryExecutionPlanConfigurer")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public ServiceRegistryExecutionPlanConfigurer dynamoDbServiceRegistryExecutionPlanConfigurer(
        @Qualifier("dynamoDbServiceRegistry")
        final ServiceRegistry dynamoDbServiceRegistry) {
        return plan -> plan.registerServiceRegistry(dynamoDbServiceRegistry);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "amazonDynamoDbServiceRegistryClient")
    public DynamoDbClient amazonDynamoDbServiceRegistryClient(final CasConfigurationProperties casProperties) {
        val dynamoDbProperties = casProperties.getServiceRegistry().getDynamoDb();
        val factory = new AmazonDynamoDbClientFactory();
        return factory.createAmazonDynamoDb(dynamoDbProperties);
    }
}
