package org.apereo.cas.config;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.adaptors.gauth.CouchDbGoogleAuthenticatorTokenCredentialRepository;
import org.apereo.cas.adaptors.gauth.GoogleAuthenticatorCouchDbTokenRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.couchdb.OneTimeTokenAccountCouchDbRepository;
import org.apereo.cas.couchdb.OneTimeTokenCouchDbRepository;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.otp.repository.token.OneTimeTokenRepository;

import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
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
    @Qualifier("oneTimeTokenCouchDbFactory")
    private CouchDbConnectorFactory oneTimeTokenCouchDbFactory;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ObjectMapperFactory objectMapperFactory;

    @ConditionalOnMissingBean(name = "oneTimeTokenCouchDbInstance")
    @RefreshScope
    @Bean
    public CouchDbInstance oneTimeTokenCouchDbInstance() {
        return oneTimeTokenCouchDbFactory.createInstance();
    }

    @ConditionalOnMissingBean(name = "oneTimeTokenCouchDbConnector")
    @RefreshScope
    @Bean
    public CouchDbConnector oneTimeTokenCouchDbConnector() {
        return oneTimeTokenCouchDbFactory.createConnector();
    }

    @ConditionalOnMissingBean(name = "oneTimeTokenAccountCouchDbFactory")
    @Bean
    @RefreshScope
    public CouchDbConnectorFactory oneTimeTokenAccountCouchDbFactory() {
        return new CouchDbConnectorFactory(casProperties.getAuthn().getMfa().getGauth().getCouchDb(), objectMapperFactory);
    }

    @ConditionalOnMissingBean(name = "couchDbGoogleAuthenticatotAccountRegistry")
    @Bean
    @RefreshScope
    public OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry(@Qualifier("googleAuthenticatorInstance")
                                                                                    final IGoogleAuthenticator googleAuthenticatorInstance,
                                                                                @Qualifier("googleAuthenticatorAccountCipherExecutor")
                                                                                    final CipherExecutor googleAuthenticatorAccountCipherExecutor) {
        return new CouchDbGoogleAuthenticatorTokenCredentialRepository(googleAuthenticatorInstance, couchDbOneTimeTokenAccountRepository(),
            googleAuthenticatorAccountCipherExecutor);
    }

    @ConditionalOnMissingBean(name = "couchDbOneTimeTokenAccountRepository")
    @Bean
    @RefreshScope
    public OneTimeTokenAccountCouchDbRepository couchDbOneTimeTokenAccountRepository() {
        val repository = new OneTimeTokenAccountCouchDbRepository(oneTimeTokenAccountCouchDbFactory().create(),
            casProperties.getAuthn().getMfa().getGauth().getCouchDb().isCreateIfNotExists());
        repository.initStandardDesignDocument();
        return repository;
    }

    @ConditionalOnMissingBean(name = "couchDbOneTimeTokenAutneticatorTokenRepository")
    @Bean
    @RefreshScope
    public OneTimeTokenRepository oneTimeTokenAuthenticatorTokenRepository() {
        return new GoogleAuthenticatorCouchDbTokenRepository(couchDbOneTimeTokenRepository(),
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
    public OneTimeTokenCouchDbRepository couchDbOneTimeTokenRepository() {
        val repository = new OneTimeTokenCouchDbRepository(oneTimeTokenCouchDbFactory.create(),
            casProperties.getAuthn().getMfa().getGauth().getCouchDb().isCreateIfNotExists());
        repository.initStandardDesignDocument();
        return repository;
    }
}
