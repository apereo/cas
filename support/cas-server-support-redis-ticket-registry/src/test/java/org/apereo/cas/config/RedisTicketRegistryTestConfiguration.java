package org.apereo.cas.config;

import org.apereo.cas.redis.core.RedisObjectFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * This is {@link RedisTicketRegistryTestConfiguration}.
 *
 * @author Fireborn Z
 * @since 6.5.0
 */
@ConditionalOnBean(name = "redisTicketConnectionFactory")
@Configuration(value = "RedisTicketRegistryTestConfiguration")
public class RedisTicketRegistryTestConfiguration {
    @Bean(name = {"stringRedisTemplate"})
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "stringRedisTemplate")
    public RedisTemplate<String, String> stringRedisTemplate(
            @Qualifier("redisTicketConnectionFactory")
            final RedisConnectionFactory redisTicketConnectionFactory) {
        return RedisObjectFactory.newRedisTemplate(redisTicketConnectionFactory);
    }
}
