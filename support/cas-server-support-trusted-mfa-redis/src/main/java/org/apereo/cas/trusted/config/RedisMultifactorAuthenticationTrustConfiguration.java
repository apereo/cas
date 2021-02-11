package org.apereo.cas.trusted.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.redis.core.RedisObjectFactory;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecordKeyGenerator;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.authentication.storage.RedisMultifactorAuthenticationTrustStorage;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

/**
 * This is {@link RedisMultifactorAuthenticationTrustConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Configuration("RedisMultifactorAuthenticationTrustConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.authn.mfa.trusted.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RedisMultifactorAuthenticationTrustConfiguration {

    @Autowired
    @Qualifier("mfaTrustRecordKeyGenerator")
    private ObjectProvider<MultifactorAuthenticationTrustRecordKeyGenerator> keyGenerationStrategy;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("mfaTrustCipherExecutor")
    private ObjectProvider<CipherExecutor> mfaTrustCipherExecutor;

    @Bean
    @ConditionalOnMissingBean(name = "redisMfaTrustedConnectionFactory")
    @RefreshScope
    public RedisConnectionFactory redisMfaTrustedConnectionFactory() {
        val redis = casProperties.getAuthn().getMfa().getTrusted().getRedis();
        return RedisObjectFactory.newRedisConnectionFactory(redis);
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "redisMfaTrustedAuthnTemplate")
    public RedisTemplate<String, List<MultifactorAuthenticationTrustRecord>> redisMfaTrustedAuthnTemplate() {
        return RedisObjectFactory.newRedisTemplate(redisMfaTrustedConnectionFactory());
    }

    @RefreshScope
    @Bean
    public MultifactorAuthenticationTrustStorage mfaTrustEngine() {
        return new RedisMultifactorAuthenticationTrustStorage(
            casProperties.getAuthn().getMfa().getTrusted(),
            mfaTrustCipherExecutor.getObject(),
            redisMfaTrustedAuthnTemplate(),
            keyGenerationStrategy.getObject());
    }
}
