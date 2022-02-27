package org.apereo.cas.config;

import org.apereo.cas.adaptors.u2f.storage.U2FDeviceRepository;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.CasFeatureModule;
import org.apereo.cas.redis.core.CasRedisTemplate;
import org.apereo.cas.redis.core.RedisObjectFactory;
import org.apereo.cas.u2f.redis.U2FRedisDeviceRepository;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeature;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * This is {@link U2FRedisConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "U2fRedisConfiguration", proxyBeanMethods = false)
@ConditionalOnFeature(feature = CasFeatureModule.FeatureCatalog.U2F, module = "redis")
public class U2FRedisConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.mfa.u2f.redis.enabled").isTrue().evenIfMissing();

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "u2fRedisTemplate")
    public CasRedisTemplate u2fRedisTemplate(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("u2fRedisConnectionFactory")
        final RedisConnectionFactory u2fRedisConnectionFactory) throws Exception {
        return BeanSupplier.of(CasRedisTemplate.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> RedisObjectFactory.newRedisTemplate(u2fRedisConnectionFactory))
            .otherwiseProxy()
            .get();
    }

    @Bean
    @ConditionalOnMissingBean(name = "u2fRedisConnectionFactory")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public RedisConnectionFactory u2fRedisConnectionFactory(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(CasSSLContext.BEAN_NAME)
        final CasSSLContext casSslContext,
        final CasConfigurationProperties casProperties) throws Exception {

        return BeanSupplier.of(RedisConnectionFactory.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val redis = casProperties.getAuthn().getMfa().getU2f().getRedis();
                return RedisObjectFactory.newRedisConnectionFactory(redis, casSslContext);
            })
            .otherwiseProxy()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public U2FDeviceRepository u2fDeviceRepository(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier("u2fRedisTemplate")
        final CasRedisTemplate u2fRedisTemplate,
        @Qualifier("u2fRegistrationRecordCipherExecutor")
        final CipherExecutor u2fRegistrationRecordCipherExecutor) throws Exception {

        return BeanSupplier.of(U2FDeviceRepository.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val u2f = casProperties.getAuthn().getMfa().getU2f();
                final LoadingCache<String, String> requestStorage =
                    Caffeine.newBuilder().expireAfterWrite(u2f.getCore().getExpireRegistrations(),
                        u2f.getCore().getExpireRegistrationsTimeUnit()).build(key -> StringUtils.EMPTY);
                return new U2FRedisDeviceRepository(requestStorage, u2fRedisTemplate,
                    u2fRegistrationRecordCipherExecutor, casProperties);
            })
            .otherwiseProxy()
            .get();
    }
}
