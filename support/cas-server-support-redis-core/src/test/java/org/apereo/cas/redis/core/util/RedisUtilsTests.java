package org.apereo.cas.redis.core.util;

import lombok.val;
import org.apereo.cas.redis.core.config.CasRedisServerConfiguration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This is {@link RedisUtilsTests}.
 *
 * @author Fireborn Z
 * @since 6.5.0
 */
@SpringBootTest(classes = {
    CasRedisServerConfiguration.class
})
@Tag("Redis")
@EnabledIfPortOpen(port = 6379)
public class RedisUtilsTests {
    @Autowired
    @Qualifier("stringRedisTemplate")
    protected RedisTemplate<String, String> stringRedisTemplate;

    @Test
    public void verifyKeys() {
        val redisKeyPrefix = "CAS_TEST:";
        val keySize = 10;
        IntStream.range(0, keySize).forEach(i -> stringRedisTemplate.boundValueOps(redisKeyPrefix + i).set("TEST"));
        val keys = RedisUtils.keys(stringRedisTemplate, redisKeyPrefix + "*");
        assertEquals(keySize, keys.count());
    }
}
