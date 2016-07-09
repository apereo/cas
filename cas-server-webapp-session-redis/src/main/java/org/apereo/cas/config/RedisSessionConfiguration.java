package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * This is {@link RedisSessionConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("redisSessionConfiguration")
@EnableRedisHttpSession
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class RedisSessionConfiguration {
}
