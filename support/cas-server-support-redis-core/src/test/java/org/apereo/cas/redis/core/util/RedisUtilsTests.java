package org.apereo.cas.redis.core.util;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.redis.core.RedisObjectFactory;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import java.util.Objects;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RedisUtilsTests}.
 *
 * @author Fireborn Z
 * @since 6.5.0
 */
@SpringBootTest(classes = RefreshAutoConfiguration.class,
    properties = {
        "cas.audit.redis.host=localhost",
        "cas.audit.redis.port=6379"
    })
@Tag("Redis")
@EnabledIfPortOpen(port = 6379)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class RedisUtilsTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    public void verifyKeys() {
        val connection = RedisObjectFactory.newRedisConnectionFactory(casProperties.getAudit().getRedis(), true,
            CasSSLContext.disabled());
        val template = RedisObjectFactory.<String, Object>newRedisTemplate(Objects.requireNonNull(connection));
        template.afterPropertiesSet();
        val redisKeyPrefix = "CAS_TEST:";
        val keySize = 10;
        val scanCount = 1000;
        IntStream.range(0, keySize).forEach(i -> template.boundValueOps(redisKeyPrefix + i).set("TEST"));
        val keys = RedisUtils.keys(template, redisKeyPrefix + '*', scanCount);
        assertEquals(keySize, keys.count());
    }
}
