package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.CasFeatureModule;
import org.apereo.cas.consent.ConsentRepository;
import org.apereo.cas.consent.DynamoDbConsentFacilitator;
import org.apereo.cas.consent.DynamoDbConsentRepository;
import org.apereo.cas.dynamodb.AmazonDynamoDbClientFactory;
import org.apereo.cas.util.spring.boot.ConditionalOnFeature;

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
 * This is {@link CasConsentDynamoDbConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeature(feature = CasFeatureModule.FeatureCatalog.Consent, module = "dynamodb")
@AutoConfiguration
public class CasConsentDynamoDbConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public ConsentRepository consentRepository(
        @Qualifier("dynamoDbConsentFacilitator")
        final DynamoDbConsentFacilitator dynamoDbConsentFacilitator) {
        return new DynamoDbConsentRepository(dynamoDbConsentFacilitator);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "amazonDynamoDbConsentClient")
    public DynamoDbClient amazonDynamoDbConsentClient(final CasConfigurationProperties casProperties) {
        val db = casProperties.getConsent().getDynamoDb();
        val factory = new AmazonDynamoDbClientFactory();
        return factory.createAmazonDynamoDb(db);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "dynamoDbConsentFacilitator")
    public DynamoDbConsentFacilitator dynamoDbConsentFacilitator(
        @Qualifier("amazonDynamoDbConsentClient")
        final DynamoDbClient amazonDynamoDbConsentClient,
        final CasConfigurationProperties casProperties) {
        val db = casProperties.getConsent().getDynamoDb();
        val f = new DynamoDbConsentFacilitator(db, amazonDynamoDbConsentClient);
        if (!db.isPreventTableCreationOnStartup()) {
            f.createTable(db.isDropTablesOnStartup());
        }
        return f;
    }

}
