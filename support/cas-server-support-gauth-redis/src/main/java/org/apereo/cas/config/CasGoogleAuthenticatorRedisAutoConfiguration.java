package org.apereo.cas.config;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.gauth.CasGoogleAuthenticator;
import org.apereo.cas.gauth.credential.RedisGoogleAuthenticatorTokenCredentialRepository;
import org.apereo.cas.gauth.token.GoogleAuthenticatorRedisTokenRepository;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.otp.repository.token.OneTimeTokenRepository;
import org.apereo.cas.redis.core.CasRedisTemplate;
import org.apereo.cas.redis.core.RedisObjectFactory;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * This is {@link CasGoogleAuthenticatorRedisAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = false)
@EnableScheduling
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.GoogleAuthenticator, module = "redis")
@AutoConfiguration
public class CasGoogleAuthenticatorRedisAutoConfiguration {
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
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(RedisConnectionFactory.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(Unchecked.supplier(() -> {
                val redis = casProperties.getAuthn().getMfa().getGauth().getRedis();
                return RedisObjectFactory.newRedisConnectionFactory(redis, casSslContext);
            }))
            .otherwiseProxy()
            .get();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "redisAccountsGoogleAuthenticatorTemplate")
    public CasRedisTemplate redisAccountsGoogleAuthenticatorTemplate(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("redisGoogleAuthenticatorConnectionFactory")
        final RedisConnectionFactory redisGoogleAuthenticatorConnectionFactory) {
        return BeanSupplier.of(CasRedisTemplate.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> RedisObjectFactory.newRedisTemplate(redisGoogleAuthenticatorConnectionFactory))
            .otherwiseProxy()
            .get();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "redisPrincipalsGoogleAuthenticatorTemplate")
    public CasRedisTemplate redisPrincipalsGoogleAuthenticatorTemplate(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("redisGoogleAuthenticatorConnectionFactory")
        final RedisConnectionFactory redisGoogleAuthenticatorConnectionFactory) {
        return BeanSupplier.of(CasRedisTemplate.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> RedisObjectFactory.newRedisTemplate(redisGoogleAuthenticatorConnectionFactory))
            .otherwiseProxy()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "redisGoogleAuthenticatorAccountRegistry")
    public OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(CasGoogleAuthenticator.BEAN_NAME)
        final CasGoogleAuthenticator googleAuthenticatorInstance,
        @Qualifier("googleAuthenticatorAccountCipherExecutor")
        final CipherExecutor googleAuthenticatorAccountCipherExecutor,
        @Qualifier("googleAuthenticatorScratchCodesCipherExecutor")
        final CipherExecutor googleAuthenticatorScratchCodesCipherExecutor,
        @Qualifier("redisAccountsGoogleAuthenticatorTemplate")
        final CasRedisTemplate redisAccountsGoogleAuthenticatorTemplate,
        @Qualifier("redisPrincipalsGoogleAuthenticatorTemplate")
        final CasRedisTemplate redisPrincipalsGoogleAuthenticatorTemplate,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(OneTimeTokenCredentialRepository.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val redisTemplates = new RedisGoogleAuthenticatorTokenCredentialRepository.CasRedisTemplates(
                    redisAccountsGoogleAuthenticatorTemplate, redisPrincipalsGoogleAuthenticatorTemplate);
                return new RedisGoogleAuthenticatorTokenCredentialRepository(googleAuthenticatorInstance, redisTemplates,
                    googleAuthenticatorAccountCipherExecutor, googleAuthenticatorScratchCodesCipherExecutor);
            })
            .otherwiseProxy()
            .get();

    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public OneTimeTokenRepository oneTimeTokenAuthenticatorTokenRepository(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier("redisAccountsGoogleAuthenticatorTemplate")
        final CasRedisTemplate redisAccountsGoogleAuthenticatorTemplate) {
        return BeanSupplier.of(OneTimeTokenRepository.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val gauth = casProperties.getAuthn().getMfa().getGauth();
                return new GoogleAuthenticatorRedisTokenRepository(redisAccountsGoogleAuthenticatorTemplate,
                    gauth.getCore().getTimeStepSize(), gauth.getRedis().getScanCount());
            })
            .otherwiseProxy()
            .get();
    }
}
