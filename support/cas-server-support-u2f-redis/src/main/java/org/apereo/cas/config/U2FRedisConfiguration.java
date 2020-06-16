package org.apereo.cas.config;

import org.apereo.cas.adaptors.u2f.storage.U2FDeviceRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.redis.core.RedisObjectFactory;
import org.apereo.cas.u2f.redis.U2FRedisDeviceRepository;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * This is {@link U2FRedisConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("u2fRedisConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class U2FRedisConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("u2fRegistrationRecordCipherExecutor")
    private ObjectProvider<CipherExecutor> u2fRegistrationRecordCipherExecutor;

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "u2fRedisTemplate")
    public RedisTemplate u2fRedisTemplate() {
        return RedisObjectFactory.newRedisTemplate(u2fRedisConnectionFactory());
    }

    @Bean
    @ConditionalOnMissingBean(name = "u2fRedisConnectionFactory")
    public RedisConnectionFactory u2fRedisConnectionFactory() {
        val redis = casProperties.getAuthn().getMfa().getU2f().getRedis();
        return RedisObjectFactory.newRedisConnectionFactory(redis);
    }

    @Bean
    public U2FDeviceRepository u2fDeviceRepository() {
        val u2f = casProperties.getAuthn().getMfa().getU2f();
        final LoadingCache<String, String> requestStorage = Caffeine.newBuilder()
            .expireAfterWrite(u2f.getExpireRegistrations(), u2f.getExpireRegistrationsTimeUnit())
            .build(key -> StringUtils.EMPTY);
        val repo = new U2FRedisDeviceRepository(requestStorage, u2fRedisTemplate(), u2f.getExpireDevices(),
            u2f.getExpireDevicesTimeUnit());
        repo.setCipherExecutor(u2fRegistrationRecordCipherExecutor.getObject());
        return repo;
    }
}
