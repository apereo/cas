package org.apereo.cas.config;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.dao.CouchDbYubiKeyAccountRegistry;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.couchdb.yubikey.YubiKeyAccountCouchDbRepository;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.val;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.impl.ObjectMapperFactory;
import org.springframework.beans.factory.ObjectProvider;
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
    private ObjectProvider<CouchDbConnectorFactory> yubikeyCouchDbFactory;

    @Autowired
    @Qualifier("yubiKeyAccountValidator")
    private ObjectProvider<YubiKeyAccountValidator> yubiKeyAccountValidator;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("yubikeyAccountCipherExecutor")
    private ObjectProvider<CipherExecutor> yubikeyAccountCipherExecutor;

    @Autowired
    @Qualifier("defaultObjectMapperFactory")
    private ObjectProvider<ObjectMapperFactory> objectMapperFactory;

    @ConditionalOnMissingBean(name = "couchDbYubiKeyAccountRepository")
    @Bean
    @RefreshScope
    public YubiKeyAccountCouchDbRepository couchDbYubiKeyAccountRepository() {
        val couchDb = casProperties.getAuthn().getMfa().getYubikey().getCouchDb();
        return new YubiKeyAccountCouchDbRepository(yubikeyCouchDbFactory.getObject().getCouchDbConnector(),
            couchDb.isCreateIfNotExists());
    }

    @ConditionalOnMissingBean(name = "yubikeyCouchDbInstance")
    @RefreshScope
    @Bean
    public CouchDbInstance yubikeyCouchDbInstance() {
        return yubikeyCouchDbFactory.getObject().getCouchDbInstance();
    }

    @ConditionalOnMissingBean(name = "yubikeyCouchDbConnector")
    @RefreshScope
    @Bean
    public CouchDbConnector yubikeyCouchDbConnector() {
        return yubikeyCouchDbFactory.getObject().getCouchDbConnector();
    }

    @ConditionalOnMissingBean(name = "yubikeyCouchDbFactory")
    @Bean
    @RefreshScope
    public CouchDbConnectorFactory yubikeyCouchDbFactory() {
        return new CouchDbConnectorFactory(casProperties.getAuthn().getMfa().getYubikey().getCouchDb(), objectMapperFactory.getObject());
    }

    @ConditionalOnMissingBean(name = "couchDbYubikeyAccountRegistry")
    @RefreshScope
    @Bean
    public YubiKeyAccountRegistry yubiKeyAccountRegistry() {
        val registry = new CouchDbYubiKeyAccountRegistry(yubiKeyAccountValidator.getObject(), couchDbYubiKeyAccountRepository());
        registry.setCipherExecutor(yubikeyAccountCipherExecutor.getObject());
        return registry;
    }
}
