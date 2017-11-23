package org.apereo.cas.config;

import org.apereo.cas.adaptors.u2f.storage.U2FMongoDbDeviceRepository;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.adaptors.u2f.storage.U2FDeviceRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.U2FMultifactorProperties;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * This is {@link U2FMongoDbConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("u2fMongoDbConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class U2FMongoDbConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;
    
    @Bean
    public U2FDeviceRepository u2fDeviceRepository() {
        final U2FMultifactorProperties u2f = casProperties.getAuthn().getMfa().getU2f();
        
        final MongoDbConnectionFactory factory = new MongoDbConnectionFactory();
        final U2FMultifactorProperties.MongoDb mongoProps = u2f.getMongo();
        final MongoTemplate mongoTemplate = factory.buildMongoTemplate(mongoProps);

        factory.createCollection(mongoTemplate, mongoProps.getCollection(), mongoProps.isDropCollection());
        
        final LoadingCache<String, String> requestStorage =
                Caffeine.newBuilder()
                        .expireAfterWrite(u2f.getExpireRegistrations(), u2f.getExpireRegistrationsTimeUnit())
                        .build(key -> StringUtils.EMPTY);
        return new U2FMongoDbDeviceRepository(requestStorage, mongoTemplate, u2f.getExpireRegistrations(), 
                u2f.getExpireDevicesTimeUnit(), mongoProps.getCollection());
    }

}
