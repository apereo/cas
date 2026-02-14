package org.apereo.cas.redis.modules;

import module java.base;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.redis.BaseRedisProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import io.lettuce.core.search.arguments.NumericFieldArgs;
import io.lettuce.core.search.arguments.TextFieldArgs;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableScheduling;
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

    @Nested
    class DefaultTests {
        @Test
        void verifyConnectionWithUsernamePassword() throws Throwable {
            val props = new BaseRedisProperties();
            props.setHost("localhost");
            props.setPort(16389);
            props.setUsername("default");
            props.setPassword("pAssw0rd123");
            val connection = LettuceRedisModulesOperations.newRediSearchCommands(props, CasSSLContext.disabled());
            assertNotNull(connection);
        }

        @Test
        void verifyRedisSearchCommandSupported() throws Throwable {
            val props = new BaseRedisProperties();
            props.setHost("localhost");
            props.setPort(6379);
            val command = LettuceRedisModulesOperations.newRediSearchCommands(props, CasSSLContext.disabled());
            val indexName = UUID.randomUUID().toString();
            val result = command.ftCreate(indexName,
                List.of(TextFieldArgs.builder().name("name").build(), NumericFieldArgs.builder().name("id").build()));
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
            val connection = LettuceRedisModulesOperations.newRediSearchCommands(props, CasSSLContext.disabled());
            assertNotNull(connection);
            assertDoesNotThrow(connection::ftList);
        }

        @Test
        void verifyConnectionWithPassword() {
            val props = new BaseRedisProperties();
            props.setHost("localhost");
            props.setPort(16389);
            props.setUsername(null);
            props.setPassword("pAssw0rd123");
            assertDoesNotThrow(() -> LettuceRedisModulesOperations.newRediSearchCommands(props, CasSSLContext.system()));
        }
    }

    @Nested
    @SpringBootTest(
        classes = AopAutoConfiguration.class,
        properties = {
            "cas.ticket.registry.redis.protocol-version=RESP2",
            "cas.ticket.registry.redis.host=localhost",
            "cas.ticket.registry.redis.port=6379",
            "cas.ticket.registry.redis.read-from=MASTER",

            "cas.ticket.registry.redis.sentinel.master=mymaster",
            "cas.ticket.registry.redis.sentinel.password=password456",
            "cas.ticket.registry.redis.sentinel.node[0]=localhost:26379",
            "cas.ticket.registry.redis.sentinel.node[1]=localhost:26380",
            "cas.ticket.registry.redis.sentinel.node[2]=localhost:26381"
        })
    @EnableScheduling
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ExtendWith(CasTestExtension.class)
    class SentinelTests {
        @Autowired
        private CasConfigurationProperties casProperties;

        @Test
        void verifySentinelConfig() throws Throwable {
            val redis = casProperties.getTicket().getRegistry().getRedis();
            val connection = LettuceRedisModulesOperations.newRediSearchCommands(redis, CasSSLContext.disabled());
            assertNotNull(connection);
        }
    }
}
