package org.apereo.cas.config;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.dao.MongoDbYubiKeyAccountRegistry;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.mongodb.core.MongoOperations;

/**
 * This is {@link CasMongoDbYubiKeyAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.YubiKey, module = "mongo")
@AutoConfiguration
public class CasMongoDbYubiKeyAutoConfiguration {

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public PersistenceExceptionTranslationPostProcessor persistenceExceptionTranslationPostProcessor() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public MongoOperations mongoYubiKeyTemplate(
        @Qualifier(CasSSLContext.BEAN_NAME)
        final CasSSLContext casSslContext,
        final CasConfigurationProperties casProperties) {
        val mongo = casProperties.getAuthn().getMfa().getYubikey().getMongo();
        val factory = new MongoDbConnectionFactory(casSslContext.getSslContext());
        val mongoTemplate = factory.buildMongoTemplate(mongo);
        MongoDbConnectionFactory.createCollection(mongoTemplate, mongo.getCollection(), mongo.isDropCollection());
        return mongoTemplate;
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public YubiKeyAccountRegistry yubiKeyAccountRegistry(
        @Qualifier("yubiKeyAccountValidator")
        final YubiKeyAccountValidator yubiKeyAccountValidator,
        @Qualifier("yubikeyAccountCipherExecutor")
        final CipherExecutor yubikeyAccountCipherExecutor,
        @Qualifier("mongoYubiKeyTemplate")
        final MongoOperations mongoYubiKeyTemplate,
        final CasConfigurationProperties casProperties) {
        val yubi = casProperties.getAuthn().getMfa().getYubikey();
        val registry = new MongoDbYubiKeyAccountRegistry(yubiKeyAccountValidator,
            mongoYubiKeyTemplate,
            yubi.getMongo().getCollection());
        registry.setCipherExecutor(yubikeyAccountCipherExecutor);
        return registry;
    }
}
