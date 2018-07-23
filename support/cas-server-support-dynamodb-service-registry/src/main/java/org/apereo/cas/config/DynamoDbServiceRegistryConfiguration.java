package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.dynamodb.AmazonDynamoDbClientFactory;
import org.apereo.cas.services.DynamoDbServiceRegistry;
import org.apereo.cas.services.DynamoDbServiceRegistryFacilitator;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlan;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link DynamoDbServiceRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("dynamoDbServiceRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class DynamoDbServiceRegistryConfiguration implements ServiceRegistryExecutionPlanConfigurer {
    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @Bean
    public DynamoDbServiceRegistryFacilitator dynamoDbServiceRegistryFacilitator() {
        val db = casProperties.getServiceRegistry().getDynamoDb();
        return new DynamoDbServiceRegistryFacilitator(db, amazonDynamoDbClient());
    }

    @Bean
    @RefreshScope
    public ServiceRegistry dynamoDbServiceRegistry() {
        return new DynamoDbServiceRegistry(dynamoDbServiceRegistryFacilitator());
    }

    @Override
    public void configureServiceRegistry(final ServiceRegistryExecutionPlan plan) {
        plan.registerServiceRegistry(dynamoDbServiceRegistry());
    }

    @RefreshScope
    @Bean
    @SneakyThrows
    public AmazonDynamoDB amazonDynamoDbClient() {
        val dynamoDbProperties = casProperties.getServiceRegistry().getDynamoDb();
        val factory = new AmazonDynamoDbClientFactory();
        return factory.createAmazonDynamoDb(dynamoDbProperties);
    }
}
