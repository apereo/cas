package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.redis.core.RedisObjectFactory;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.webauthn.RedisWebAuthnCredentialRegistration;
import org.apereo.cas.webauthn.RedisWebAuthnCredentialRepository;
import org.apereo.cas.webauthn.storage.WebAuthnCredentialRepository;

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
 * This is {@link RedisWebAuthnConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Configuration("RedisWebAuthnConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class RedisWebAuthnConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("webAuthnCredentialRegistrationCipherExecutor")
    private ObjectProvider<CipherExecutor> webAuthnCredentialRegistrationCipherExecutor;

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "webAuthnRedisTemplate")
    public RedisTemplate<String, RedisWebAuthnCredentialRegistration> webAuthnRedisTemplate() {
        return RedisObjectFactory.newRedisTemplate(webAuthnRedisConnectionFactory());
    }

    @Bean
    @ConditionalOnMissingBean(name = "webAuthnRedisConnectionFactory")
    @RefreshScope
    public RedisConnectionFactory webAuthnRedisConnectionFactory() {
        val redis = casProperties.getAuthn().getMfa().getWebAuthn().getRedis();
        return RedisObjectFactory.newRedisConnectionFactory(redis);
    }

    @RefreshScope
    @Bean
    public WebAuthnCredentialRepository webAuthnCredentialRepository() {
        return new RedisWebAuthnCredentialRepository(webAuthnRedisTemplate(),
            casProperties, webAuthnCredentialRegistrationCipherExecutor.getObject());
    }
}
