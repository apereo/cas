package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.redis.core.RedisObjectFactory;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.webauthn.RedisWebAuthnCredentialRegistration;
import org.apereo.cas.webauthn.RedisWebAuthnCredentialRepository;
import org.apereo.cas.webauthn.storage.WebAuthnCredentialRepository;

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
 * This is {@link RedisWebAuthnConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.authn.mfa.web-authn.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
@Configuration(value = "RedisWebAuthnConfiguration", proxyBeanMethods = false)
public class RedisWebAuthnConfiguration {

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "webAuthnRedisTemplate")
    public RedisTemplate<String, RedisWebAuthnCredentialRegistration> webAuthnRedisTemplate(
        @Qualifier("webAuthnRedisConnectionFactory")
        final RedisConnectionFactory webAuthnRedisConnectionFactory) {
        return RedisObjectFactory.newRedisTemplate(webAuthnRedisConnectionFactory);
    }

    @Bean
    @ConditionalOnMissingBean(name = "webAuthnRedisConnectionFactory")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public RedisConnectionFactory webAuthnRedisConnectionFactory(final CasConfigurationProperties casProperties) {
        val redis = casProperties.getAuthn().getMfa().getWebAuthn().getRedis();
        return RedisObjectFactory.newRedisConnectionFactory(redis);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @Autowired
    public WebAuthnCredentialRepository webAuthnCredentialRepository(final CasConfigurationProperties casProperties,
                                                                     @Qualifier("webAuthnRedisTemplate")
                                                                     final RedisTemplate<String, RedisWebAuthnCredentialRegistration> webAuthnRedisTemplate,
                                                                     @Qualifier("webAuthnCredentialRegistrationCipherExecutor")
                                                                     final CipherExecutor webAuthnCredentialRegistrationCipherExecutor) {
        return new RedisWebAuthnCredentialRepository(webAuthnRedisTemplate,
            casProperties,
            webAuthnCredentialRegistrationCipherExecutor);
    }
}
