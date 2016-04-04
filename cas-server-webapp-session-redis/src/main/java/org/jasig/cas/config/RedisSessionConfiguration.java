package org.jasig.cas.config;

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
public class RedisSessionConfiguration {
}
