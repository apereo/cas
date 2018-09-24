package org.apereo.cas.config;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.couchdb.trusted.MultifactorAuthenticationTrustRecordCouchDbRepository;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.authentication.storage.CouchDbMultifactorAuthenticationTrustStorage;

import lombok.val;
import org.ektorp.impl.ObjectMapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CouchDbMultifactorAuthenticationTrustConfiguration}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Configuration("couchDbMultifactorAuthenticationTrustConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CouchDbMultifactorAuthenticationTrustConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("mfaTrustCipherExecutor")
    private CipherExecutor mfaTrustCipherExecutor;

    @Autowired
    @Qualifier("defaultObjectMapperFactory")
    private ObjectMapperFactory objectMapperFactory;

    @ConditionalOnMissingBean(name = "mfaTrustCouchDbFactory")
    @Bean
    @RefreshScope
    public CouchDbConnectorFactory mfaTrustCouchDbFactory() {
        return new CouchDbConnectorFactory(casProperties.getAuthn().getMfa().getTrusted().getCouchDb(), objectMapperFactory);
    }

    @ConditionalOnMissingBean(name = "couchDbTrustRecordRepository")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationTrustRecordCouchDbRepository couchDbTrustRecordRepository(
        @Qualifier("mfaTrustCouchDbFactory") final CouchDbConnectorFactory mfaTrustCouchDbFactory) {
        return new MultifactorAuthenticationTrustRecordCouchDbRepository(mfaTrustCouchDbFactory.getCouchDbConnector(),
            casProperties.getAuthn().getMfa().getTrusted().getCouchDb().isCreateIfNotExists());
    }

    @ConditionalOnMissingBean(name = "couchDbMfaTrustEngine")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationTrustStorage mfaTrustEngine(
        @Qualifier("couchDbTrustRecordRepository") final MultifactorAuthenticationTrustRecordCouchDbRepository couchDbTrustRecordRepository) {
        val c = new CouchDbMultifactorAuthenticationTrustStorage(couchDbTrustRecordRepository);
        c.setCipherExecutor(this.mfaTrustCipherExecutor);
        return c;
    }
}
