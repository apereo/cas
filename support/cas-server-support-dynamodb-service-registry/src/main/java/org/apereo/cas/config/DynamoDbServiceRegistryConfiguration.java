package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.dynamodb.AmazonDynamoDbClientFactory;
import org.apereo.cas.services.DynamoDbServiceRegistry;
import org.apereo.cas.services.DynamoDbServiceRegistryFacilitator;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServiceRegistryListener;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import lombok.SneakyThrows;
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
 * This is {@link DynamoDbServiceRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("dynamoDbServiceRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class DynamoDbServiceRegistryConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("serviceRegistryListeners")
    private ObjectProvider<Collection<ServiceRegistryListener>> serviceRegistryListeners;

    @RefreshScope
    @Bean
    public DynamoDbServiceRegistryFacilitator dynamoDbServiceRegistryFacilitator() {
        val db = casProperties.getServiceRegistry().getDynamoDb();
        return new DynamoDbServiceRegistryFacilitator(db, amazonDynamoDbServiceRegistryClient());
    }

    @Bean
    @RefreshScope
    public ServiceRegistry dynamoDbServiceRegistry() {
        return new DynamoDbServiceRegistry(applicationContext, dynamoDbServiceRegistryFacilitator(), serviceRegistryListeners.getObject());
    }

    @Bean
    @ConditionalOnMissingBean(name = "dynamoDbServiceRegistryExecutionPlanConfigurer")
    public ServiceRegistryExecutionPlanConfigurer dynamoDbServiceRegistryExecutionPlanConfigurer() {
        return plan -> plan.registerServiceRegistry(dynamoDbServiceRegistry());
    }

    @RefreshScope
    @Bean
    @SneakyThrows
    @ConditionalOnMissingBean(name = "amazonDynamoDbServiceRegistryClient")
    public AmazonDynamoDB amazonDynamoDbServiceRegistryClient() {
        val dynamoDbProperties = casProperties.getServiceRegistry().getDynamoDb();
        val factory = new AmazonDynamoDbClientFactory();
        return factory.createAmazonDynamoDb(dynamoDbProperties);
    }
}
