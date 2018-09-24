package org.apereo.cas.config;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.adaptors.gauth.CouchDbGoogleAuthenticatorTokenCredentialRepository;
import org.apereo.cas.adaptors.gauth.GoogleAuthenticatorCouchDbTokenRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.couchdb.gauth.OneTimeTokenAccountCouchDbRepository;
import org.apereo.cas.couchdb.gauth.OneTimeTokenCouchDbRepository;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.otp.repository.token.OneTimeTokenRepository;

import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.ektorp.impl.ObjectMapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * This is {@link GoogleAuthenticatorCouchDbConfiguration}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Configuration("googleAuthenticatorCouchDbConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableScheduling
@Slf4j
public class GoogleAuthenticatorCouchDbConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("defaultObjectMapperFactory")
    private ObjectMapperFactory objectMapperFactory;

    @ConditionalOnMissingBean(name = "oneTimeTokenAccountCouchDbFactory")
    @Bean
    @RefreshScope
    public CouchDbConnectorFactory oneTimeTokenAccountCouchDbFactory() {
        return new CouchDbConnectorFactory(casProperties.getAuthn().getMfa().getGauth().getCouchDb(), objectMapperFactory);
    }

    @ConditionalOnMissingBean(name = "couchDbGoogleAuthenticatotAccountRegistry")
    @Bean
    @RefreshScope
    public OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry(
        @Qualifier("googleAuthenticatorInstance") final IGoogleAuthenticator googleAuthenticatorInstance,
        @Qualifier("googleAuthenticatorAccountCipherExecutor") final CipherExecutor googleAuthenticatorAccountCipherExecutor,
        @Qualifier("couchDbOneTimeTokenAccountRepository") final OneTimeTokenAccountCouchDbRepository couchDbRepository) {
        return new CouchDbGoogleAuthenticatorTokenCredentialRepository(googleAuthenticatorInstance, couchDbRepository,
            googleAuthenticatorAccountCipherExecutor);
    }

    @ConditionalOnMissingBean(name = "couchDbOneTimeTokenAccountRepository")
    @Bean
    @RefreshScope
    public OneTimeTokenAccountCouchDbRepository couchDbOneTimeTokenAccountRepository(
        @Qualifier("oneTimeTokenAccountCouchDbFactory") final CouchDbConnectorFactory oneTimeTokenCouchDbFactory) {
        val repository = new OneTimeTokenAccountCouchDbRepository(oneTimeTokenCouchDbFactory.getCouchDbConnector(),
            casProperties.getAuthn().getMfa().getGauth().getCouchDb().isCreateIfNotExists());
        repository.initStandardDesignDocument();
        return repository;
    }

    @ConditionalOnMissingBean(name = "couchDbOneTimeTokenAutneticatorTokenRepository")
    @Bean
    @RefreshScope
    public OneTimeTokenRepository oneTimeTokenAuthenticatorTokenRepository(
        @Qualifier("couchDbOneTimeTokenRepository") final OneTimeTokenCouchDbRepository couchDbOneTimeTokenRepository) {
        return new GoogleAuthenticatorCouchDbTokenRepository(couchDbOneTimeTokenRepository,
            casProperties.getAuthn().getMfa().getGauth().getTimeStepSize());
    }

    @ConditionalOnMissingBean(name = "oneTimeTokenCouchDbFactory")
    @Bean
    @RefreshScope
    public CouchDbConnectorFactory oneTimeTokenCouchDbFactory() {
        return new CouchDbConnectorFactory(casProperties.getAuthn().getMfa().getGauth().getCouchDb(), objectMapperFactory);
    }

    @ConditionalOnMissingBean(name = "couchDbbOneTimeTokenRepository")
    @Bean
    @RefreshScope
    public OneTimeTokenCouchDbRepository couchDbOneTimeTokenRepository(
        @Qualifier("oneTimeTokenAccountCouchDbFactory") final CouchDbConnectorFactory oneTimeTokenCouchDbFactory) {
        val repository = new OneTimeTokenCouchDbRepository(oneTimeTokenCouchDbFactory.getCouchDbConnector(),
            casProperties.getAuthn().getMfa().getGauth().getCouchDb().isCreateIfNotExists());
        repository.initStandardDesignDocument();
        return repository;
    }
}
