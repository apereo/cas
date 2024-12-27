package org.apereo.cas.redis.modules;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.model.support.redis.BaseRedisProperties;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import com.redis.lettucemod.search.Field;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RedisModulesOperationsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Tag("Redis")
@EnabledIfListeningOnPort(port = 6379)
class RedisModulesOperationsTests {
    @Test
    void verifyConnectionWithUsernamePassword() throws Throwable {
        val props = new BaseRedisProperties();
        props.setHost("localhost");
        props.setPort(16389);
        props.setUsername("default");
        props.setPassword("pAssw0rd123");
        val connection = LettuceRedisModulesOperations.newRedisModulesCommands(props, CasSSLContext.disabled());
        assertNotNull(connection);
    }

    @Test
    void verifyRedisSearchCommandSupported() throws Throwable {
        val props = new BaseRedisProperties();
        props.setHost("localhost");
        props.setPort(6379);
        val command = LettuceRedisModulesOperations.newRedisModulesCommands(props, CasSSLContext.disabled());
        val indexName = UUID.randomUUID().toString();
        val result = command.ftCreate(indexName,
            Field.text("name").build(),
            Field.numeric("id").build());
        assertEquals("OK", result);
        val info = command.ftInfo(indexName);
        assertNotNull(info);
    }

    @Test
    void verifyConnectionWithUsernamePasswordOverTls() throws Throwable {
        val props = new BaseRedisProperties();
        props.setHost("localhost");
        props.setPort(16669);
        props.setUsername("default");
        props.setPassword("pAssw0rd123");
        props.setKeyCertificateChainFile(new File("../../ci/tests/redis/certs/redis.crt"));
        props.setKeyFile(new File("../../ci/tests/redis/certs/redis.key"));
        props.setVerifyPeer(false);
        props.setUseSsl(true);
        val connection = LettuceRedisModulesOperations.newRedisModulesCommands(props, CasSSLContext.disabled());
        assertNotNull(connection);
        try (val con = connection.getStatefulConnection()) {
            assertTrue(con.isOpen());
        }
    }

    @Test
    void verifyConnectionWithPassword() {
        val props = new BaseRedisProperties();
        props.setHost("localhost");
        props.setPort(16389);
        props.setUsername(null);
        props.setPassword("pAssw0rd123");
        assertDoesNotThrow(() -> LettuceRedisModulesOperations.newRedisModulesCommands(props, CasSSLContext.system()));
    }
}
