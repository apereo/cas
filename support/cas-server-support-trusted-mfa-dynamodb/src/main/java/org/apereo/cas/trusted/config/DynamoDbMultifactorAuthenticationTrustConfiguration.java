package org.apereo.cas.trusted.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.dynamodb.AmazonDynamoDbClientFactory;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecordKeyGenerator;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.authentication.storage.DynamoDbMultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.authentication.storage.DynamoDbMultifactorTrustEngineFacilitator;
import org.apereo.cas.util.crypto.CipherExecutor;

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
 * This is {@link DynamoDbMultifactorAuthenticationTrustConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "dynamoDbMultifactorAuthenticationTrustConfiguration", proxyBeanMethods = false)
public class DynamoDbMultifactorAuthenticationTrustConfiguration {

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "amazonDynamoDbMultifactorTrustEngineClient")
    @Autowired
    public DynamoDbClient amazonDynamoDbMultifactorTrustEngineClient(final CasConfigurationProperties casProperties) {
        val db = casProperties.getAuthn().getMfa().getTrusted().getDynamoDb();
        val factory = new AmazonDynamoDbClientFactory();
        return factory.createAmazonDynamoDb(db);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "dynamoDbMultifactorTrustEngineFacilitator")
    @Autowired
    public DynamoDbMultifactorTrustEngineFacilitator dynamoDbMultifactorTrustEngineFacilitator(final CasConfigurationProperties casProperties,
                                                                                               @Qualifier("amazonDynamoDbMultifactorTrustEngineClient")
                                                                                               final DynamoDbClient amazonDynamoDbMultifactorTrustEngineClient) {
        val db = casProperties.getAuthn().getMfa().getTrusted().getDynamoDb();
        val f = new DynamoDbMultifactorTrustEngineFacilitator(db, amazonDynamoDbMultifactorTrustEngineClient);
        if (!db.isPreventTableCreationOnStartup()) {
            f.createTable(db.isDropTablesOnStartup());
        }
        return f;
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @Autowired
    public MultifactorAuthenticationTrustStorage mfaTrustEngine(
        final CasConfigurationProperties casProperties,
        @Qualifier("dynamoDbMultifactorTrustEngineFacilitator")
        final DynamoDbMultifactorTrustEngineFacilitator dynamoDbMultifactorTrustEngineFacilitator,
        @Qualifier("mfaTrustRecordKeyGenerator")
        final MultifactorAuthenticationTrustRecordKeyGenerator keyGenerationStrategy,
        @Qualifier("mfaTrustCipherExecutor")
        final CipherExecutor mfaTrustCipherExecutor) {
        return new DynamoDbMultifactorAuthenticationTrustStorage(casProperties.getAuthn().getMfa().getTrusted(), mfaTrustCipherExecutor, dynamoDbMultifactorTrustEngineFacilitator,
            keyGenerationStrategy);
    }
}
