package org.apereo.cas.config;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.redis.core.CasRedisTemplate;
import org.apereo.cas.redis.core.RedisObjectFactory;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.webauthn.RedisWebAuthnCredentialRegistration;
import org.apereo.cas.webauthn.RedisWebAuthnCredentialRepository;
import org.apereo.cas.webauthn.storage.WebAuthnCredentialRepository;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * This is {@link CasRedisWebAuthnAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.WebAuthn)
@AutoConfiguration
public class CasRedisWebAuthnAutoConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.mfa.web-authn.redis.enabled").isTrue().evenIfMissing();

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "webAuthnRedisTemplate")
    public CasRedisTemplate<String, RedisWebAuthnCredentialRegistration> webAuthnRedisTemplate(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("webAuthnRedisConnectionFactory")
        final RedisConnectionFactory webAuthnRedisConnectionFactory) {
        return BeanSupplier.of(CasRedisTemplate.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> RedisObjectFactory.newRedisTemplate(webAuthnRedisConnectionFactory))
            .otherwiseProxy()
            .get();
    }

    @Bean
    @ConditionalOnMissingBean(name = "webAuthnRedisConnectionFactory")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public RedisConnectionFactory webAuthnRedisConnectionFactory(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(CasSSLContext.BEAN_NAME)
        final CasSSLContext casSslContext,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(RedisConnectionFactory.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(Unchecked.supplier(() -> {
                val redis = casProperties.getAuthn().getMfa().getWebAuthn().getRedis();
                return RedisObjectFactory.newRedisConnectionFactory(redis, casSslContext);
            }))
            .otherwiseProxy()
            .get();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public WebAuthnCredentialRepository webAuthnCredentialRepository(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier("webAuthnRedisTemplate")
        final CasRedisTemplate<String, RedisWebAuthnCredentialRegistration> webAuthnRedisTemplate,
        @Qualifier("webAuthnCredentialRegistrationCipherExecutor")
        final CipherExecutor webAuthnCredentialRegistrationCipherExecutor) {
        return BeanSupplier.of(WebAuthnCredentialRepository.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> new RedisWebAuthnCredentialRepository(webAuthnRedisTemplate,
                casProperties, webAuthnCredentialRegistrationCipherExecutor))
            .otherwiseProxy()
            .get();
    }
}
