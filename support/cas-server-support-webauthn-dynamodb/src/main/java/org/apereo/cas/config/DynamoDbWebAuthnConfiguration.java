package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.dynamodb.AmazonDynamoDbClientFactory;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.webauthn.DynamoDbWebAuthnCredentialRepository;
import org.apereo.cas.webauthn.DynamoDbWebAuthnFacilitator;
import org.apereo.cas.webauthn.storage.WebAuthnCredentialRepository;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * This is {@link DynamoDbWebAuthnConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Configuration("DynamoDbWebAuthnConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class DynamoDbWebAuthnConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("webAuthnCredentialRegistrationCipherExecutor")
    private ObjectProvider<CipherExecutor> webAuthnCredentialRegistrationCipherExecutor;

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "amazonDynamoDbWebAuthnClient")
    public DynamoDbClient amazonDynamoDbWebAuthnClient() {
        val db = casProperties.getAuthn().getMfa().getWebAuthn().getDynamoDb();
        val factory = new AmazonDynamoDbClientFactory();
        return factory.createAmazonDynamoDb(db);
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "dynamoDbWebAuthnFacilitator")
    public DynamoDbWebAuthnFacilitator dynamoDbWebAuthnFacilitator() {
        val db = casProperties.getAuthn().getMfa().getWebAuthn().getDynamoDb();
        val f = new DynamoDbWebAuthnFacilitator(db, amazonDynamoDbWebAuthnClient());
        if (!db.isPreventTableCreationOnStartup()) {
            f.createTable(db.isDropTablesOnStartup());
        }
        return f;
    }

    @RefreshScope
    @Bean
    public WebAuthnCredentialRepository webAuthnCredentialRepository() {
        return new DynamoDbWebAuthnCredentialRepository(casProperties,
            webAuthnCredentialRegistrationCipherExecutor.getObject(),
            dynamoDbWebAuthnFacilitator());
    }
}
