package org.apereo.cas.config;

import org.apereo.cas.adaptors.u2f.storage.U2FDeviceRepository;
import org.apereo.cas.adaptors.u2f.storage.U2FDynamoDbDeviceRepository;
import org.apereo.cas.adaptors.u2f.storage.U2FDynamoDbFacilitator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.dynamodb.AmazonDynamoDbClientFactory;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
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
 * This is {@link U2FDynamoDbConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Configuration(value = "u2fDynamoDbConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class U2FDynamoDbConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("u2fRegistrationRecordCipherExecutor")
    private ObjectProvider<CipherExecutor> u2fRegistrationRecordCipherExecutor;

    @RefreshScope
    @Bean
    @Autowired
    public U2FDynamoDbFacilitator u2fDynamoDbFacilitator(@Qualifier("u2fDynamoDbClient") final DynamoDbClient u2fDynamoDbClient) {
        val db = casProperties.getAuthn().getMfa().getU2f().getDynamoDb();
        val f = new U2FDynamoDbFacilitator(db, u2fDynamoDbClient);
        if (!db.isPreventTableCreationOnStartup()) {
            f.createTable(db.isDropTablesOnStartup());
        }
        return f;
    }

    @RefreshScope
    @Bean
    @SneakyThrows
    @ConditionalOnMissingBean(name = "u2fDynamoDbClient")
    public DynamoDbClient u2fDynamoDbClient() {
        val db = casProperties.getAuthn().getMfa().getU2f().getDynamoDb();
        val factory = new AmazonDynamoDbClientFactory();
        return factory.createAmazonDynamoDb(db);
    }

    @Bean
    @RefreshScope
    @Autowired
    public U2FDeviceRepository u2fDeviceRepository(@Qualifier("u2fDynamoDbFacilitator") final U2FDynamoDbFacilitator u2fDynamoDbFacilitator) {
        val u2f = casProperties.getAuthn().getMfa().getU2f();
        final LoadingCache<String, String> requestStorage =
            Caffeine.newBuilder()
                .expireAfterWrite(u2f.getExpireRegistrations(), u2f.getExpireRegistrationsTimeUnit())
                .build(key -> StringUtils.EMPTY);
        return new U2FDynamoDbDeviceRepository(requestStorage, u2fRegistrationRecordCipherExecutor.getObject(),
            u2f.getExpireDevices(), u2f.getExpireDevicesTimeUnit(), u2fDynamoDbFacilitator);
    }
}
