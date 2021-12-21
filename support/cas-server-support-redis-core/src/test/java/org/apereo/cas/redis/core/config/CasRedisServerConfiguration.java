package org.apereo.cas.redis.core.config;

import lombok.val;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.model.support.redis.BaseRedisProperties;
import org.apereo.cas.redis.core.RedisObjectFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Objects;

/**
 * This is {@link CasRedisServerConfiguration}.
 *
 * @author Fireborn Z
 * @since 6.5.0
 */
@Configuration
public class CasRedisServerConfiguration {
    @ConditionalOnMissingBean(name = "redisProperties")
    @Bean(name = "redisProperties")
    public BaseRedisProperties redisProperties() {
        val props = new BaseRedisProperties();
        props.setHost("localhost");
        props.setPort(6379);
        return props;
    }

    @ConditionalOnMissingBean(name = "stringRedisTemplate")
    @Bean(name = "stringRedisTemplate")
    public RedisTemplate<String, String> stringRedisTemplate(
        @Qualifier("redisProperties") BaseRedisProperties redisProperties) {
        val connection = RedisObjectFactory.newRedisConnectionFactory(redisProperties, true, CasSSLContext.disabled());
        return RedisObjectFactory.newRedisTemplate(Objects.requireNonNull(connection));
    }
}
