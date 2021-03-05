package org.apereo.cas.config;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.dao.DynamoDbYubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.dao.DynamoDbYubiKeyFacilitator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.dynamodb.AmazonDynamoDbClientFactory;
import org.apereo.cas.util.crypto.CipherExecutor;

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
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * This is {@link DynamoDbYubiKeyConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Configuration("dynamoDbYubiKeyConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class DynamoDbYubiKeyConfiguration {
    @Autowired
    @Qualifier("yubiKeyAccountValidator")
    private ObjectProvider<YubiKeyAccountValidator> yubiKeyAccountValidator;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("yubikeyAccountCipherExecutor")
    private ObjectProvider<CipherExecutor> yubikeyAccountCipherExecutor;

    @RefreshScope
    @Bean
    public DynamoDbYubiKeyFacilitator yubikeyDynamoDbFacilitator() {
        val db = casProperties.getAuthn().getMfa().getYubikey().getDynamoDb();
        val f = new DynamoDbYubiKeyFacilitator(db, yubikeyDynamoDbClient());
        if (!db.isPreventTableCreationOnStartup()) {
            f.createTable(db.isDropTablesOnStartup());
        }
        return f;
    }

    @RefreshScope
    @Bean
    @SneakyThrows
    @ConditionalOnMissingBean(name = "yubikeyDynamoDbClient")
    public DynamoDbClient yubikeyDynamoDbClient() {
        val db = casProperties.getAuthn().getMfa().getYubikey().getDynamoDb();
        val factory = new AmazonDynamoDbClientFactory();
        return factory.createAmazonDynamoDb(db);
    }

    @RefreshScope
    @Bean
    public YubiKeyAccountRegistry yubiKeyAccountRegistry() {
        val registry = new DynamoDbYubiKeyAccountRegistry(yubiKeyAccountValidator.getObject(), yubikeyDynamoDbFacilitator());
        registry.setCipherExecutor(yubikeyAccountCipherExecutor.getObject());
        return registry;
    }

}
