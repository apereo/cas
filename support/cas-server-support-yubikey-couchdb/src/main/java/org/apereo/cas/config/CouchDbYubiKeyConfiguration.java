package org.apereo.cas.config;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.dao.CouchDbYubiKeyAccountRegistry;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.couchdb.yubikey.YubiKeyAccountCouchDbRepository;

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

/**
 * This is {@link CouchDbYubiKeyConfiguration}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Configuration("couchDbYubiKeyConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CouchDbYubiKeyConfiguration {

    @Autowired
    @Qualifier("yubikeyCouchDbFactory")
    private CouchDbConnectorFactory yubikeyCouchDbFactory;

    @Autowired
    @Qualifier("yubiKeyAccountValidator")
    private YubiKeyAccountValidator yubiKeyAccountValidator;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("yubikeyAccountCipherExecutor")
    private CipherExecutor yubikeyAccountCipherExecutor;

    @Autowired
    @Qualifier("defaultObjectMapperFactory")
    private ObjectMapperFactory objectMapperFactory;

    @ConditionalOnMissingBean(name = "couchDbYubiKeyAccountRepository")
    @Bean
    @RefreshScope
    public YubiKeyAccountCouchDbRepository couchDbYubiKeyAccountRepository() {
        val couchDb = casProperties.getAuthn().getMfa().getYubikey().getCouchDb();
        return new YubiKeyAccountCouchDbRepository(yubikeyCouchDbFactory.getCouchDbConnector(),
            couchDb.isCreateIfNotExists());
    }

    @ConditionalOnMissingBean(name = "yubikeyCouchDbInstance")
    @RefreshScope
    @Bean
    public CouchDbInstance yubikeyCouchDbInstance() {
        return yubikeyCouchDbFactory.getCouchDbInstance();
    }

    @ConditionalOnMissingBean(name = "yubikeyCouchDbConnector")
    @RefreshScope
    @Bean
    public CouchDbConnector yubikeyCouchDbConnector() {
        return yubikeyCouchDbFactory.getCouchDbConnector();
    }

    @ConditionalOnMissingBean(name = "yubikeyCouchDbFactory")
    @Bean
    @RefreshScope
    public CouchDbConnectorFactory yubikeyCouchDbFactory() {
        return new CouchDbConnectorFactory(casProperties.getAuthn().getMfa().getYubikey().getCouchDb(), objectMapperFactory);
    }

    @ConditionalOnMissingBean(name = "couchDbYubikeyAccountRegistry")
    @RefreshScope
    @Bean
    public YubiKeyAccountRegistry yubiKeyAccountRegistry() {
        val registry = new CouchDbYubiKeyAccountRegistry(yubiKeyAccountValidator, couchDbYubiKeyAccountRepository());
        registry.setCipherExecutor(yubikeyAccountCipherExecutor);
        return registry;
    }
}
