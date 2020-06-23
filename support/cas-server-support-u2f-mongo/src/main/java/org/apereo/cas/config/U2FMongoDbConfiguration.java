package org.apereo.cas.config;

import org.apereo.cas.adaptors.u2f.storage.U2FDeviceRepository;
import org.apereo.cas.adaptors.u2f.storage.U2FMongoDbDeviceRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;

/**
 * This is {@link U2FMongoDbConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration(value = "u2fMongoDbConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class U2FMongoDbConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("u2fRegistrationRecordCipherExecutor")
    private ObjectProvider<CipherExecutor> u2fRegistrationRecordCipherExecutor;

    @Autowired
    @Qualifier("sslContext")
    private ObjectProvider<SSLContext> sslContext;

    @Bean
    @RefreshScope
    public U2FDeviceRepository u2fDeviceRepository() {
        val u2f = casProperties.getAuthn().getMfa().getU2f();

        val factory = new MongoDbConnectionFactory(sslContext.getObject());
        val mongoProps = u2f.getMongo();
        val mongoTemplate = factory.buildMongoTemplate(mongoProps);

        MongoDbConnectionFactory.createCollection(mongoTemplate, mongoProps.getCollection(), mongoProps.isDropCollection());
        final LoadingCache<String, String> requestStorage =
            Caffeine.newBuilder()
                .expireAfterWrite(u2f.getExpireRegistrations(), u2f.getExpireRegistrationsTimeUnit())
                .build(key -> StringUtils.EMPTY);
        val repo = new U2FMongoDbDeviceRepository(requestStorage, mongoTemplate, u2f.getExpireDevices(),
            u2f.getExpireDevicesTimeUnit(), mongoProps.getCollection());
        repo.setCipherExecutor(u2fRegistrationRecordCipherExecutor.getObject());
        return repo;
    }

}
