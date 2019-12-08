package org.apereo.cas.config;

import org.apereo.cas.adaptors.u2f.storage.U2FCouchDbDeviceRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.U2FMultifactorProperties;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.couchdb.u2f.U2FDeviceRegistrationCouchDbRepository;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.ektorp.impl.ObjectMapperFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.Serializable;

/**
 * This is {@link U2FCouchDbConfiguration}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Configuration(value = "couchDbU2fConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class U2FCouchDbConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("u2fRegistrationRecordCipherExecutor")
    private CipherExecutor<Serializable, String> u2fRegistrationRecordCipherExecutor;

    @Autowired
    @Qualifier("defaultObjectMapperFactory")
    private ObjectProvider<ObjectMapperFactory> objectMapperFactory;

    @ConditionalOnMissingBean(name = "u2fCouchDbFactory")
    @Bean
    @RefreshScope
    public CouchDbConnectorFactory u2fCouchDbFactory() {
        return new CouchDbConnectorFactory(casProperties.getAuthn().getMfa().getU2f().getCouchDb(), objectMapperFactory.getObject());
    }

    @ConditionalOnMissingBean(name = "couchDbU2fDeviceRegistrationRepository")
    @Bean
    @RefreshScope
    public U2FDeviceRegistrationCouchDbRepository couchDbU2fDeviceRegistrationRepository(
        @Qualifier("u2fCouchDbFactory") final CouchDbConnectorFactory u2fCouchDbFactory) {
        final U2FMultifactorProperties.CouchDb couchDb = casProperties.getAuthn().getMfa().getU2f().getCouchDb();
        return new U2FDeviceRegistrationCouchDbRepository(u2fCouchDbFactory.getCouchDbConnector(),
            u2fCouchDbFactory.getCouchDbInstance(),
            couchDb.isCreateIfNotExists());
    }

    @ConditionalOnMissingBean(name = "couchDbU2fDeviceRepository")
    @Bean
    @RefreshScope
    public U2FCouchDbDeviceRepository u2fDeviceRepository(
        @Qualifier("couchDbU2fDeviceRegistrationRepository") final U2FDeviceRegistrationCouchDbRepository couchDbU2fDeviceRegistrationRepository) {
        val u2f = casProperties.getAuthn().getMfa().getU2f();
        val couchDb = u2f.getCouchDb();

        final LoadingCache<String, String> requestStorage =
            Caffeine.newBuilder()
                .expireAfterWrite(u2f.getExpireRegistrations(), u2f.getExpireRegistrationsTimeUnit())
                .build(key -> StringUtils.EMPTY);
        val repo = new U2FCouchDbDeviceRepository(requestStorage, couchDbU2fDeviceRegistrationRepository,
            u2f.getExpireRegistrations(),
            u2f.getExpireDevicesTimeUnit(),
            couchDb.isAsynchronous());
        repo.setCipherExecutor(this.u2fRegistrationRecordCipherExecutor);
        return repo;
    }
}
