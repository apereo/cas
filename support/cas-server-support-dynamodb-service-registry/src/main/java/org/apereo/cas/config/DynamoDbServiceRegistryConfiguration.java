package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.dynamodb.AmazonDynamoDbClientFactory;
import org.apereo.cas.services.DynamoDbServiceRegistry;
import org.apereo.cas.services.DynamoDbServiceRegistryFacilitator;
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
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link DynamoDbServiceRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "dynamoDbServiceRegistryConfiguration", proxyBeanMethods = false)
public class DynamoDbServiceRegistryConfiguration {

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @Autowired
    public DynamoDbServiceRegistryFacilitator dynamoDbServiceRegistryFacilitator(final CasConfigurationProperties casProperties,
                                                                                 @Qualifier("amazonDynamoDbServiceRegistryClient")
                                                                                 final DynamoDbClient amazonDynamoDbServiceRegistryClient) {
        val db = casProperties.getServiceRegistry().getDynamoDb();
        return new DynamoDbServiceRegistryFacilitator(db, amazonDynamoDbServiceRegistryClient);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "dynamoDbServiceRegistry")
    @Autowired
    public ServiceRegistry dynamoDbServiceRegistry(final ConfigurableApplicationContext applicationContext,
                                                   final ObjectProvider<List<ServiceRegistryListener>> serviceRegistryListeners,
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
    @Autowired
    public DynamoDbClient amazonDynamoDbServiceRegistryClient(final CasConfigurationProperties casProperties) {
        val dynamoDbProperties = casProperties.getServiceRegistry().getDynamoDb();
        val factory = new AmazonDynamoDbClientFactory();
        return factory.createAmazonDynamoDb(dynamoDbProperties);
    }
}
