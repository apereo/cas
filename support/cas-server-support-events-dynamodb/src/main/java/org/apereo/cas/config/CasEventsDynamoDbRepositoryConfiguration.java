package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.dynamodb.AmazonDynamoDbClientFactory;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.CasEventRepositoryFilter;
import org.apereo.cas.support.events.DynamoDbCasEventRepository;
import org.apereo.cas.support.events.DynamoDbCasEventsFacilitator;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * This is {@link CasEventsDynamoDbRepositoryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Configuration(value = "CasEventsInfluxDbRepositoryConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasEventsDynamoDbRepositoryConfiguration {

    @Bean
    public CasEventRepository casEventRepository(
        @Qualifier("dynamoDbEventRepositoryFilter")
        final CasEventRepositoryFilter dynamoDbEventRepositoryFilter,
        @Qualifier("dynamoDbCasEventsFacilitator")
        final DynamoDbCasEventsFacilitator dynamoDbCasEventsFacilitator) {
        return new DynamoDbCasEventRepository(dynamoDbEventRepositoryFilter, dynamoDbCasEventsFacilitator);
    }

    @ConditionalOnMissingBean(name = "dynamoDbEventRepositoryFilter")
    @Bean
    public CasEventRepositoryFilter dynamoDbEventRepositoryFilter() {
        return CasEventRepositoryFilter.noOp();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @Autowired
    public DynamoDbCasEventsFacilitator dynamoDbCasEventsFacilitator(
        @Qualifier("dynamoDbEventRepositoryClient")
        final DynamoDbClient dynamoDbEventRepositoryClient,
        final CasConfigurationProperties casProperties) {
        val db = casProperties.getEvents().getDynamoDb();
        val f = new DynamoDbCasEventsFacilitator(db, dynamoDbEventRepositoryClient);
        if (!db.isPreventTableCreationOnStartup()) {
            f.createTable(db.isDropTablesOnStartup());
        }
        return f;
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "dynamoDbEventRepositoryClient")
    @Autowired
    public DynamoDbClient dynamoDbEventRepositoryClient(final CasConfigurationProperties casProperties) {
        val dynamoDbProperties = casProperties.getEvents().getDynamoDb();
        val factory = new AmazonDynamoDbClientFactory();
        return factory.createAmazonDynamoDb(dynamoDbProperties);
    }
}
