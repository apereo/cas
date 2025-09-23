package org.apereo.cas.redis.core.util;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.redis.core.RedisObjectFactory;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = RefreshAutoConfiguration.class, properties = {
    "cas.audit.redis.host=localhost",
    "cas.audit.redis.port=6379"
})
@Tag("Redis")
@ExtendWith(CasTestExtension.class)
@EnabledIfListeningOnPort(port = 6379)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class RedisUtilsTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    void verifyKeys() throws Throwable {
        val connection = RedisObjectFactory.newRedisConnectionFactory(casProperties.getAudit().getRedis(), true,
            CasSSLContext.disabled());
        val template = RedisObjectFactory.<String, Object>newRedisTemplate(Objects.requireNonNull(connection));
        template.initialize();
        val redisKeyPrefix = "CAS_TEST:";
        val keySize = 10;
        IntStream.range(0, keySize).forEach(i -> template.boundValueOps(redisKeyPrefix + i).set("TEST"));
        try (val keys = template.scan(redisKeyPrefix + '*', 1000L)) {
            assertEquals(keySize, keys.count());
        }
    }
}
