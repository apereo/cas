package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.gauth.credential.RedisGoogleAuthenticatorTokenCredentialRepository;
import org.apereo.cas.gauth.token.GoogleAuthenticatorRedisTokenRepository;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.otp.repository.token.OneTimeTokenRepository;
import org.apereo.cas.redis.core.RedisObjectFactory;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.warrenstrange.googleauth.IGoogleAuthenticator;
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
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * This is {@link GoogleAuthenticatorRedisConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement
@EnableScheduling
@ConditionalOnProperty(prefix = "cas.authn.mfa.gauth.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
@Configuration(value = "googleAuthenticatorRedisConfiguration", proxyBeanMethods = false)
public class GoogleAuthenticatorRedisConfiguration {

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public PersistenceExceptionTranslationPostProcessor persistenceExceptionTranslationPostProcessor() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    @Bean
    @ConditionalOnMissingBean(name = "redisGoogleAuthenticatorConnectionFactory")
    @Autowired
    public RedisConnectionFactory redisGoogleAuthenticatorConnectionFactory(final CasConfigurationProperties casProperties) {
        val redis = casProperties.getAuthn().getMfa().getGauth().getRedis();
        return RedisObjectFactory.newRedisConnectionFactory(redis);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "redisGoogleAuthenticatorTemplate")
    public RedisTemplate redisGoogleAuthenticatorTemplate(
        @Qualifier("redisGoogleAuthenticatorConnectionFactory")
        final RedisConnectionFactory redisGoogleAuthenticatorConnectionFactory) {
        return RedisObjectFactory.newRedisTemplate(redisGoogleAuthenticatorConnectionFactory);
    }

    @Autowired
    @Bean
    public OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry(
        @Qualifier("googleAuthenticatorInstance")
        final IGoogleAuthenticator googleAuthenticatorInstance,
        @Qualifier("googleAuthenticatorAccountCipherExecutor")
        final CipherExecutor googleAuthenticatorAccountCipherExecutor,
        @Qualifier("redisGoogleAuthenticatorTemplate")
        final RedisTemplate redisGoogleAuthenticatorTemplate) {
        return new RedisGoogleAuthenticatorTokenCredentialRepository(googleAuthenticatorInstance, redisGoogleAuthenticatorTemplate, googleAuthenticatorAccountCipherExecutor);
    }

    @Bean
    @Autowired
    public OneTimeTokenRepository oneTimeTokenAuthenticatorTokenRepository(final CasConfigurationProperties casProperties,
                                                                           @Qualifier("redisGoogleAuthenticatorTemplate")
                                                                           final RedisTemplate redisGoogleAuthenticatorTemplate) {
        return new GoogleAuthenticatorRedisTokenRepository(redisGoogleAuthenticatorTemplate, casProperties.getAuthn().getMfa().getGauth().getCore().getTimeStepSize());
    }
}
