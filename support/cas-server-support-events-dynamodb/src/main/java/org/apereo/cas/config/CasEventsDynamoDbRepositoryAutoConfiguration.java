package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.dynamodb.AmazonDynamoDbClientFactory;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.CasEventRepositoryFilter;
import org.apereo.cas.support.events.DynamoDbCasEventRepository;
import org.apereo.cas.support.events.DynamoDbCasEventsFacilitator;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * This is {@link CasEventsDynamoDbRepositoryAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Events, module = "dynamodb")
@AutoConfiguration
public class CasEventsDynamoDbRepositoryAutoConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasEventRepository casEventRepository(
        @Qualifier("dynamoDbEventRepositoryFilter")
        final CasEventRepositoryFilter dynamoDbEventRepositoryFilter,
        @Qualifier("dynamoDbCasEventsFacilitator")
        final DynamoDbCasEventsFacilitator dynamoDbCasEventsFacilitator) {
        return new DynamoDbCasEventRepository(dynamoDbEventRepositoryFilter, dynamoDbCasEventsFacilitator);
    }

    @ConditionalOnMissingBean(name = "dynamoDbEventRepositoryFilter")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasEventRepositoryFilter dynamoDbEventRepositoryFilter() {
        return CasEventRepositoryFilter.noOp();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public DynamoDbCasEventsFacilitator dynamoDbCasEventsFacilitator(
        @Qualifier("dynamoDbEventRepositoryClient")
        final DynamoDbClient dynamoDbEventRepositoryClient,
        final CasConfigurationProperties casProperties) throws Exception {
        val db = casProperties.getEvents().getDynamoDb();
        val facilitator = new DynamoDbCasEventsFacilitator(db, dynamoDbEventRepositoryClient);
        if (!db.isPreventTableCreationOnStartup()) {
            facilitator.createTable(db.isDropTablesOnStartup());
        }
        return facilitator;
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "dynamoDbEventRepositoryClient")
    public DynamoDbClient dynamoDbEventRepositoryClient(final CasConfigurationProperties casProperties) {
        val dynamoDbProperties = casProperties.getEvents().getDynamoDb();
        val factory = new AmazonDynamoDbClientFactory();
        return factory.createAmazonDynamoDb(dynamoDbProperties);
    }
}
