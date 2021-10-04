package org.apereo.cas.trusted.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.redis.core.RedisObjectFactory;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecordKeyGenerator;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.authentication.storage.RedisMultifactorAuthenticationTrustStorage;
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

import java.util.List;

/**
 * This is {@link RedisMultifactorAuthenticationTrustConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.authn.mfa.trusted.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
@Configuration(value = "RedisMultifactorAuthenticationTrustConfiguration", proxyBeanMethods = false)
public class RedisMultifactorAuthenticationTrustConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "redisMfaTrustedConnectionFactory")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public RedisConnectionFactory redisMfaTrustedConnectionFactory(final CasConfigurationProperties casProperties) {
        val redis = casProperties.getAuthn().getMfa().getTrusted().getRedis();
        return RedisObjectFactory.newRedisConnectionFactory(redis);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "redisMfaTrustedAuthnTemplate")
    public RedisTemplate<String, List<MultifactorAuthenticationTrustRecord>> redisMfaTrustedAuthnTemplate(
        @Qualifier("redisMfaTrustedConnectionFactory")
        final RedisConnectionFactory redisMfaTrustedConnectionFactory) {
        return RedisObjectFactory.newRedisTemplate(redisMfaTrustedConnectionFactory);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @Autowired
    public MultifactorAuthenticationTrustStorage mfaTrustEngine(
        final CasConfigurationProperties casProperties,
        @Qualifier("redisMfaTrustedAuthnTemplate")
        final RedisTemplate<String, List<MultifactorAuthenticationTrustRecord>> redisMfaTrustedAuthnTemplate,
        @Qualifier("mfaTrustRecordKeyGenerator")
        final MultifactorAuthenticationTrustRecordKeyGenerator keyGenerationStrategy,
        @Qualifier("mfaTrustCipherExecutor")
        final CipherExecutor mfaTrustCipherExecutor) {
        return new RedisMultifactorAuthenticationTrustStorage(casProperties.getAuthn().getMfa().getTrusted(),
            mfaTrustCipherExecutor, redisMfaTrustedAuthnTemplate, keyGenerationStrategy);
    }
}
