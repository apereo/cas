package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.couchdb.core.DefaultCouchDbConnectorFactory;
import org.apereo.cas.couchdb.trusted.MultifactorAuthenticationTrustRecordCouchDbRepository;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecordKeyGenerator;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.authentication.storage.CouchDbMultifactorAuthenticationTrustStorage;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import org.ektorp.impl.ObjectMapperFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CouchDbMultifactorAuthenticationTrustConfiguration}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 * @deprecated Since 7
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.MultifactorAuthenticationTrustedDevices, module = "couchdb")
@AutoConfiguration
@Deprecated(since = "7.0.0")
public class CouchDbMultifactorAuthenticationTrustConfiguration {

    @ConditionalOnMissingBean(name = "mfaTrustCouchDbFactory")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CouchDbConnectorFactory mfaTrustCouchDbFactory(
        final CasConfigurationProperties casProperties,
        @Qualifier("defaultObjectMapperFactory")
        final ObjectMapperFactory objectMapperFactory) {
        return new DefaultCouchDbConnectorFactory(
            casProperties.getAuthn().getMfa().getTrusted().getCouchDb(), objectMapperFactory);
    }

    @ConditionalOnMissingBean(name = "couchDbTrustRecordRepository")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MultifactorAuthenticationTrustRecordCouchDbRepository couchDbTrustRecordRepository(
        @Qualifier("mfaTrustCouchDbFactory")
        final CouchDbConnectorFactory mfaTrustCouchDbFactory, final CasConfigurationProperties casProperties) {
        return new MultifactorAuthenticationTrustRecordCouchDbRepository(mfaTrustCouchDbFactory.getCouchDbConnector(),
            casProperties.getAuthn().getMfa().getTrusted().getCouchDb().isCreateIfNotExists());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MultifactorAuthenticationTrustStorage mfaTrustEngine(
        @Qualifier("couchDbTrustRecordRepository")
        final MultifactorAuthenticationTrustRecordCouchDbRepository couchDbTrustRecordRepository, final CasConfigurationProperties casProperties,
        @Qualifier("mfaTrustRecordKeyGenerator")
        final MultifactorAuthenticationTrustRecordKeyGenerator keyGenerationStrategy,
        @Qualifier("mfaTrustCipherExecutor")
        final CipherExecutor mfaTrustCipherExecutor) {
        return new CouchDbMultifactorAuthenticationTrustStorage(casProperties.getAuthn().getMfa().getTrusted(), mfaTrustCipherExecutor, couchDbTrustRecordRepository, keyGenerationStrategy);
    }
}
