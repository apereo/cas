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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * This is {@link U2FRedisConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.authn.mfa.u2f.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
@Configuration(value = "u2fRedisConfiguration", proxyBeanMethods = false)
public class U2FRedisConfiguration {

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "u2fRedisTemplate")
    public RedisTemplate u2fRedisTemplate(
        @Qualifier("u2fRedisConnectionFactory")
        final RedisConnectionFactory u2fRedisConnectionFactory) {
        return RedisObjectFactory.newRedisTemplate(u2fRedisConnectionFactory);
    }

    @Bean
    @ConditionalOnMissingBean(name = "u2fRedisConnectionFactory")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public RedisConnectionFactory u2fRedisConnectionFactory(final CasConfigurationProperties casProperties) {
        val redis = casProperties.getAuthn().getMfa().getU2f().getRedis();
        return RedisObjectFactory.newRedisConnectionFactory(redis);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public U2FDeviceRepository u2fDeviceRepository(final CasConfigurationProperties casProperties,
                                                   @Qualifier("u2fRedisTemplate")
                                                   final RedisTemplate u2fRedisTemplate,
                                                   @Qualifier("u2fRegistrationRecordCipherExecutor")
                                                   final CipherExecutor u2fRegistrationRecordCipherExecutor) {
        val u2f = casProperties.getAuthn().getMfa().getU2f();
        final LoadingCache<String, String> requestStorage =
            Caffeine.newBuilder().expireAfterWrite(u2f.getCore().getExpireRegistrations(), u2f.getCore().getExpireRegistrationsTimeUnit()).build(key -> StringUtils.EMPTY);
        return new U2FRedisDeviceRepository(requestStorage, u2fRedisTemplate, u2f.getCore().getExpireDevices(), u2f.getCore().getExpireDevicesTimeUnit(),
            u2fRegistrationRecordCipherExecutor);
    }
}
