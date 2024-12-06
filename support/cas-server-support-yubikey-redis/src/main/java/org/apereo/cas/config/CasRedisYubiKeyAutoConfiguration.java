package org.apereo.cas.config;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.dao.RedisYubiKeyAccountRegistry;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.redis.core.CasRedisTemplate;
import org.apereo.cas.redis.core.RedisObjectFactory;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
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
 * This is {@link CasRedisYubiKeyAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.YubiKey, module = "redis")
@AutoConfiguration
public class CasRedisYubiKeyAutoConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.mfa.yubikey.redis.enabled").isTrue().evenIfMissing();

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "redisYubiKeyTemplate")
    public CasRedisTemplate redisYubiKeyTemplate(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("redisYubiKeyConnectionFactory")
        final RedisConnectionFactory redisYubiKeyConnectionFactory) {
        return BeanSupplier.of(CasRedisTemplate.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> RedisObjectFactory.newRedisTemplate(redisYubiKeyConnectionFactory))
            .otherwiseProxy()
            .get();
    }

    @Bean
    @ConditionalOnMissingBean(name = "redisYubiKeyConnectionFactory")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public RedisConnectionFactory redisYubiKeyConnectionFactory(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(CasSSLContext.BEAN_NAME)
        final CasSSLContext casSslContext,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(RedisConnectionFactory.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(Unchecked.supplier(() -> {
                val redis = casProperties.getAuthn().getMfa().getYubikey().getRedis();
                return RedisObjectFactory.newRedisConnectionFactory(redis, casSslContext);
            }))
            .otherwiseProxy()
            .get();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public YubiKeyAccountRegistry yubiKeyAccountRegistry(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("redisYubiKeyTemplate")
        final CasRedisTemplate redisYubiKeyTemplate,
        @Qualifier("yubiKeyAccountValidator")
        final YubiKeyAccountValidator yubiKeyAccountValidator,
        @Qualifier("yubikeyAccountCipherExecutor")
        final CipherExecutor yubikeyAccountCipherExecutor,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(YubiKeyAccountRegistry.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val registry = new RedisYubiKeyAccountRegistry(yubiKeyAccountValidator, redisYubiKeyTemplate,
                    casProperties.getAuthn().getMfa().getYubikey().getRedis().getScanCount());
                registry.setCipherExecutor(yubikeyAccountCipherExecutor);
                return registry;
            })
            .otherwiseProxy()
            .get();
    }
}
