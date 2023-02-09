package org.apereo.cas;

import org.apereo.cas.configuration.model.support.redis.BaseRedisProperties;
import org.apereo.cas.redis.core.RedisObjectFactory;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;

import com.redis.lettucemod.search.Field;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RedisObjectFactoryWithSearchTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Redis")
@EnabledIfListeningOnPort(port = 10001)
public class RedisObjectFactoryWithSearchTests {
    @Test
    public void verifyRedisSearchCommandSupported() {
        val props = new BaseRedisProperties();
        props.setHost("localhost");
        props.setPort(10001);
        val command = RedisObjectFactory.newRedisSearchCommands(props).orElseThrow();
        assertNotNull(command);
        val indexName = UUID.randomUUID().toString();
        val result = command.ftCreate(indexName,
            Field.text("name").build(),
            Field.numeric("id").build());
        assertEquals("OK", result);
        val info = command.ftInfo(indexName);
        assertNotNull(info);
    }
}
