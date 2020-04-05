package org.apereo.cas.trusted.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.dynamodb.AmazonDynamoDbClientFactory;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecordKeyGenerator;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.authentication.storage.DynamoDbMultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.authentication.storage.DynamoDbMultifactorTrustEngineFacilitator;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link DynamoDbMultifactorAuthenticationTrustConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration("dynamoDbMultifactorAuthenticationTrustConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class DynamoDbMultifactorAuthenticationTrustConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("mfaTrustRecordKeyGenerator")
    private ObjectProvider<MultifactorAuthenticationTrustRecordKeyGenerator> keyGenerationStrategy;

    @Autowired
    @Qualifier("mfaTrustCipherExecutor")
    private ObjectProvider<CipherExecutor> mfaTrustCipherExecutor;

    @RefreshScope
    @Bean
    @SneakyThrows
    @ConditionalOnMissingBean(name = "amazonDynamoDbMultifactorTrustEngineClient")
    public AmazonDynamoDB amazonDynamoDbMultifactorTrustEngineClient() {
        val db = casProperties.getAuthn().getMfa().getTrusted().getDynamoDb();
        val factory = new AmazonDynamoDbClientFactory();
        return factory.createAmazonDynamoDb(db);
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "dynamoDbMultifactorTrustEngineFacilitator")
    public DynamoDbMultifactorTrustEngineFacilitator dynamoDbMultifactorTrustEngineFacilitator() {
        val db = casProperties.getAuthn().getMfa().getTrusted().getDynamoDb();
        val f = new DynamoDbMultifactorTrustEngineFacilitator(db, amazonDynamoDbMultifactorTrustEngineClient());
        if (!db.isPreventTableCreationOnStartup()) {
            f.createTable(db.isDropTablesOnStartup());
        }
        return f;
    }

    @RefreshScope
    @Bean
    public MultifactorAuthenticationTrustStorage mfaTrustEngine() {
        return new DynamoDbMultifactorAuthenticationTrustStorage(casProperties.getAuthn().getMfa().getTrusted(),
            mfaTrustCipherExecutor.getObject(), dynamoDbMultifactorTrustEngineFacilitator(), keyGenerationStrategy.getObject());
    }
}
