package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.gauth.token.GoogleAuthenticatorRedisTokenRepository;
import org.apereo.cas.otp.repository.token.OneTimeTokenRepository;
import org.apereo.cas.redis.core.RedisObjectFactory;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
@Configuration("googleAuthenticatorRedisConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = true)
@EnableScheduling
public class GoogleAuthenticatorRedisConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @Bean
    public PersistenceExceptionTranslationPostProcessor persistenceExceptionTranslationPostProcessor() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    @Bean
    @ConditionalOnMissingBean(name = "redisGoogleAuthenticatorConnectionFactory")
    public RedisConnectionFactory redisGoogleAuthenticatorConnectionFactory() {
        val redis = casProperties.getAuthn().getMfa().getGauth().getRedis();
        return RedisObjectFactory.newRedisConnectionFactory(redis);
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "redisGoogleAuthenticatorTemplate")
    public RedisTemplate redisGoogleAuthenticatorTemplate() {
        return RedisObjectFactory.newRedisTemplate(redisGoogleAuthenticatorConnectionFactory());
    }

    /*
    @Autowired
    @Bean
    public OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry(@Qualifier("googleAuthenticatorInstance") final IGoogleAuthenticator googleAuthenticatorInstance,
                                                                               @Qualifier("googleAuthenticatorAccountCipherExecutor") final CipherExecutor googleAuthenticatorAccountCipherExecutor) {
        val mongo = casProperties.getAuthn().getMfa().getGauth().getMongo();
        return new MongoDbGoogleAuthenticatorTokenCredentialRepository(
            googleAuthenticatorInstance,
            mongoDbGoogleAuthenticatorTemplate(),
            mongo.getCollection(),
            googleAuthenticatorAccountCipherExecutor
        );
    }
    */

    @Bean
    public OneTimeTokenRepository oneTimeTokenAuthenticatorTokenRepository() {
        return new GoogleAuthenticatorRedisTokenRepository(redisGoogleAuthenticatorTemplate(),
            casProperties.getAuthn().getMfa().getGauth().getTimeStepSize());
    }
}
