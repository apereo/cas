package org.apereo.cas.config;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.dao.DynamoDbYubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.dao.DynamoDbYubiKeyFacilitator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.dynamodb.AmazonDynamoDbClientFactory;
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
 * This is {@link DynamoDbYubiKeyConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "dynamoDbYubiKeyConfiguration", proxyBeanMethods = false)
public class DynamoDbYubiKeyConfiguration {

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @Autowired
    public DynamoDbYubiKeyFacilitator yubikeyDynamoDbFacilitator(final CasConfigurationProperties casProperties,
                                                                 @Qualifier("yubikeyDynamoDbClient")
                                                                 final DynamoDbClient yubikeyDynamoDbClient) {
        val db = casProperties.getAuthn().getMfa().getYubikey().getDynamoDb();
        val f = new DynamoDbYubiKeyFacilitator(db, yubikeyDynamoDbClient);
        if (!db.isPreventTableCreationOnStartup()) {
            f.createTable(db.isDropTablesOnStartup());
        }
        return f;
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "yubikeyDynamoDbClient")
    @Autowired
    public DynamoDbClient yubikeyDynamoDbClient(final CasConfigurationProperties casProperties) {
        val db = casProperties.getAuthn().getMfa().getYubikey().getDynamoDb();
        val factory = new AmazonDynamoDbClientFactory();
        return factory.createAmazonDynamoDb(db);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public YubiKeyAccountRegistry yubiKeyAccountRegistry(
        @Qualifier("yubikeyDynamoDbFacilitator")
        final DynamoDbYubiKeyFacilitator yubikeyDynamoDbFacilitator,
        @Qualifier("yubiKeyAccountValidator")
        final YubiKeyAccountValidator yubiKeyAccountValidator,
        @Qualifier("yubikeyAccountCipherExecutor")
        final CipherExecutor yubikeyAccountCipherExecutor) {
        val registry = new DynamoDbYubiKeyAccountRegistry(yubiKeyAccountValidator, yubikeyDynamoDbFacilitator);
        registry.setCipherExecutor(yubikeyAccountCipherExecutor);
        return registry;
    }
}
