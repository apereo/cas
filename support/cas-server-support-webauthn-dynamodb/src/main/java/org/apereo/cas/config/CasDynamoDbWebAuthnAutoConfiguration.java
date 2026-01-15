package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.dynamodb.AmazonDynamoDbClientFactory;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.webauthn.DynamoDbWebAuthnCredentialRepository;
import org.apereo.cas.webauthn.DynamoDbWebAuthnFacilitator;
import org.apereo.cas.webauthn.storage.WebAuthnCredentialRepository;
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
 * This is {@link CasDynamoDbWebAuthnAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.WebAuthn)
@AutoConfiguration
public class CasDynamoDbWebAuthnAutoConfiguration {

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "amazonDynamoDbWebAuthnClient")
    public DynamoDbClient amazonDynamoDbWebAuthnClient(final CasConfigurationProperties casProperties) {
        val db = casProperties.getAuthn().getMfa().getWebAuthn().getDynamoDb();
        val factory = new AmazonDynamoDbClientFactory();
        return factory.createAmazonDynamoDb(db);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "dynamoDbWebAuthnFacilitator")
    public DynamoDbWebAuthnFacilitator dynamoDbWebAuthnFacilitator(
        final CasConfigurationProperties casProperties,
        @Qualifier("amazonDynamoDbWebAuthnClient")
        final DynamoDbClient amazonDynamoDbWebAuthnClient) {
        val db = casProperties.getAuthn().getMfa().getWebAuthn().getDynamoDb();
        val f = new DynamoDbWebAuthnFacilitator(db, amazonDynamoDbWebAuthnClient);
        if (!db.isPreventTableCreationOnStartup()) {
            f.createTable(db.isDropTablesOnStartup());
        }
        return f;
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public WebAuthnCredentialRepository webAuthnCredentialRepository(
        final CasConfigurationProperties casProperties,
        @Qualifier("dynamoDbWebAuthnFacilitator")
        final DynamoDbWebAuthnFacilitator dynamoDbWebAuthnFacilitator,
        @Qualifier("webAuthnCredentialRegistrationCipherExecutor")
        final CipherExecutor webAuthnCredentialRegistrationCipherExecutor) {
        return new DynamoDbWebAuthnCredentialRepository(casProperties, webAuthnCredentialRegistrationCipherExecutor, dynamoDbWebAuthnFacilitator);
    }
}
