package org.apereo.cas.config;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.dao.RedisYubiKeyAccountRegistry;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.redis.core.RedisObjectFactory;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.val;
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
 * This is {@link RedisYubiKeyConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Configuration("redisYubiKeyConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class RedisYubiKeyConfiguration {

    @Autowired
    @Qualifier("yubiKeyAccountValidator")
    private ObjectProvider<YubiKeyAccountValidator> yubiKeyAccountValidator;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("yubikeyAccountCipherExecutor")
    private ObjectProvider<CipherExecutor> yubikeyAccountCipherExecutor;

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "redisYubiKeyTemplate")
    public RedisTemplate redisYubiKeyTemplate() {
        return RedisObjectFactory.newRedisTemplate(redisYubiKeyConnectionFactory());
    }

    @Bean
    @ConditionalOnMissingBean(name = "redisYubiKeyConnectionFactory")
    @RefreshScope
    public RedisConnectionFactory redisYubiKeyConnectionFactory() {
        val redis = casProperties.getAuthn().getMfa().getYubikey().getRedis();
        return RedisObjectFactory.newRedisConnectionFactory(redis);
    }

    @RefreshScope
    @Bean
    public YubiKeyAccountRegistry yubiKeyAccountRegistry() {
        val registry = new RedisYubiKeyAccountRegistry(yubiKeyAccountValidator.getObject(), redisYubiKeyTemplate());
        registry.setCipherExecutor(yubikeyAccountCipherExecutor.getObject());
        return registry;
    }
}
