package org.apereo.cas.redis.core.util;

import lombok.val;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.model.support.redis.BaseRedisProperties;
import org.apereo.cas.redis.core.RedisObjectFactory;
import org.apereo.cas.util.junit.EnabledIfPortOpen;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This is {@link RedisUtilsTest}.
 *
 * @author Fireborn Z
 * @since 6.5.0
 */
@Tag("Redis")
@EnabledIfPortOpen(port = 6379)
public class RedisUtilsTest {
    private static final int REDIS_KEY_SIZE = 10;

    @Test
    public void verifyGetKeys() {
        val props = new BaseRedisProperties();
        props.setHost("localhost");
        props.setPort(6379);
        props.getPool().setMinEvictableIdleTimeMillis(2000);
        props.getPool().setNumTestsPerEvictionRun(1);
        props.getPool().setSoftMinEvictableIdleTimeMillis(1);
        val connection = RedisObjectFactory.newRedisConnectionFactory(props, true, CasSSLContext.disabled());
        RedisTemplate<String, String> redisTemplate = RedisObjectFactory.newRedisTemplate(connection);
        redisTemplate.afterPropertiesSet();
        val redisKeyPrefix = "CAS_REDIS_TEST_KEY:";
        for (var i = 0; i < REDIS_KEY_SIZE; i++) {
            redisTemplate.boundValueOps(redisKeyPrefix + i).set("CAS_REDIS_TEST_VALUE_" + i);
        }
        var keySets = RedisUtils.keys(redisTemplate, redisKeyPrefix + "*");
        assertEquals(REDIS_KEY_SIZE, keySets.size());
        for (var i = 0; i < REDIS_KEY_SIZE; i++) {
            assertTrue(keySets.contains(redisKeyPrefix + i));
        }
    }
}