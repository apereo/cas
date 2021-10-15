package org.apereo.cas.config;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.dao.RedisYubiKeyAccountRegistry;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.redis.core.RedisObjectFactory;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.val;
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
 * This is {@link RedisYubiKeyConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Configuration(value = "redisYubiKeyConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.authn.mfa.yubikey.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RedisYubiKeyConfiguration {

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "redisYubiKeyTemplate")
    @Autowired
    public RedisTemplate redisYubiKeyTemplate(
        @Qualifier("redisYubiKeyConnectionFactory")
        final RedisConnectionFactory redisYubiKeyConnectionFactory) {
        return RedisObjectFactory.newRedisTemplate(redisYubiKeyConnectionFactory);
    }

    @Bean
    @ConditionalOnMissingBean(name = "redisYubiKeyConnectionFactory")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public RedisConnectionFactory redisYubiKeyConnectionFactory(final CasConfigurationProperties casProperties) {
        val redis = casProperties.getAuthn().getMfa().getYubikey().getRedis();
        return RedisObjectFactory.newRedisConnectionFactory(redis);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @Autowired
    public YubiKeyAccountRegistry yubiKeyAccountRegistry(
        @Qualifier("redisYubiKeyTemplate")
        final RedisTemplate redisYubiKeyTemplate,
        @Qualifier("yubiKeyAccountValidator")
        final YubiKeyAccountValidator yubiKeyAccountValidator,
        @Qualifier("yubikeyAccountCipherExecutor")
        final CipherExecutor yubikeyAccountCipherExecutor) {
        val registry = new RedisYubiKeyAccountRegistry(yubiKeyAccountValidator, redisYubiKeyTemplate);
        registry.setCipherExecutor(yubikeyAccountCipherExecutor);
        return registry;
    }
}
