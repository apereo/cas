package org.apereo.cas.config;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.CasFeatureModule;
import org.apereo.cas.gauth.credential.RedisGoogleAuthenticatorTokenCredentialRepository;
import org.apereo.cas.gauth.token.GoogleAuthenticatorRedisTokenRepository;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.otp.repository.token.OneTimeTokenRepository;
import org.apereo.cas.redis.core.CasRedisTemplate;
import org.apereo.cas.redis.core.RedisObjectFactory;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeature;

import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * This is {@link GoogleAuthenticatorRedisConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = false)
@EnableScheduling
@Configuration(value = "GoogleAuthenticatorRedisConfiguration", proxyBeanMethods = false)
@ConditionalOnFeature(feature = CasFeatureModule.FeatureCatalog.GoogleAuthenticator, module = "redis")
public class GoogleAuthenticatorRedisConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.mfa.gauth.redis.enabled").isTrue().evenIfMissing();

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public BeanPostProcessor persistenceExceptionTranslationPostProcessor() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    @Bean
    @ConditionalOnMissingBean(name = "redisGoogleAuthenticatorConnectionFactory")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public RedisConnectionFactory redisGoogleAuthenticatorConnectionFactory(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(CasSSLContext.BEAN_NAME)
        final CasSSLContext casSslContext,
        final CasConfigurationProperties casProperties) throws Exception {
        return BeanSupplier.of(RedisConnectionFactory.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val redis = casProperties.getAuthn().getMfa().getGauth().getRedis();
                return RedisObjectFactory.newRedisConnectionFactory(redis, casSslContext);
            })
            .otherwiseProxy()
            .get();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "redisGoogleAuthenticatorTemplate")
    public CasRedisTemplate redisGoogleAuthenticatorTemplate(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("redisGoogleAuthenticatorConnectionFactory")
        final RedisConnectionFactory redisGoogleAuthenticatorConnectionFactory) throws Exception {
        return BeanSupplier.of(CasRedisTemplate.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> RedisObjectFactory.newRedisTemplate(redisGoogleAuthenticatorConnectionFactory))
            .otherwiseProxy()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("googleAuthenticatorInstance")
        final IGoogleAuthenticator googleAuthenticatorInstance,
        @Qualifier("googleAuthenticatorAccountCipherExecutor")
        final CipherExecutor googleAuthenticatorAccountCipherExecutor,
        @Qualifier("googleAuthenticatorScratchCodesCipherExecutor")
        final CipherExecutor googleAuthenticatorScratchCodesCipherExecutor,
        @Qualifier("redisGoogleAuthenticatorTemplate")
        final CasRedisTemplate redisGoogleAuthenticatorTemplate,
        final CasConfigurationProperties casProperties) throws Exception {
        return BeanSupplier.of(OneTimeTokenCredentialRepository.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                return new RedisGoogleAuthenticatorTokenCredentialRepository(googleAuthenticatorInstance,
                    redisGoogleAuthenticatorTemplate,
                    googleAuthenticatorAccountCipherExecutor,
                    googleAuthenticatorScratchCodesCipherExecutor,
                    casProperties.getAuthn().getMfa().getGauth().getRedis().getScanCount());
            })
            .otherwiseProxy()
            .get();

    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public OneTimeTokenRepository oneTimeTokenAuthenticatorTokenRepository(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier("redisGoogleAuthenticatorTemplate")
        final CasRedisTemplate redisGoogleAuthenticatorTemplate) throws Exception {
        return BeanSupplier.of(OneTimeTokenRepository.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> new GoogleAuthenticatorRedisTokenRepository(redisGoogleAuthenticatorTemplate,
                casProperties.getAuthn().getMfa().getGauth().getCore().getTimeStepSize(),
                casProperties.getAuthn().getMfa().getGauth().getRedis().getScanCount()))
            .otherwiseProxy()
            .get();
    }
}
