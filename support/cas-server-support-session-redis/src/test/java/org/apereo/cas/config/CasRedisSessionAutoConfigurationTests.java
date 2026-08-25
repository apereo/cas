package org.apereo.cas.config;

import module java.base;
import io.lettuce.core.api.StatefulConnection;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.session.data.redis.RedisSessionRepository;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CasRedisSessionAutoConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("Redis")
class CasRedisSessionAutoConfigurationTests {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(
            CasRedisSessionAutoConfiguration.class,
            DataRedisAutoConfiguration.class))
        .withUserConfiguration(TicketRedisConfiguration.class);

    @Test
    void verifyTicketRegistryConnectionFactoryIsUsedByDefault() {
        contextRunner.run(context -> {
            assertNull(context.getStartupFailure());
            assertTrue(context.containsBean("springSessionRedisConnectionFactory"));
            assertFalse(context.containsBean("redisConnectionFactory"));

            var ticketConnectionFactory = context.getBean(
                "redisTicketConnectionFactory", RedisConnectionFactory.class);
            var sessionConnectionFactory = context.getBean(
                "springSessionRedisConnectionFactory", RedisConnectionFactory.class);
            assertSame(ticketConnectionFactory, sessionConnectionFactory);

            var repository = context.getBean(RedisSessionRepository.class);
            var redisTemplate = (RedisTemplate<String, Object>) repository.getSessionRedisOperations();
            assertSame(ticketConnectionFactory, redisTemplate.getConnectionFactory());
        });
    }

    @Test
    void verifySpringDataRedisPropertiesCreateDedicatedSessionConnectionFactory() {
        contextRunner
            .withPropertyValues(
                "spring.data.redis.host=session-redis.example.org",
                "spring.data.redis.port=6380",
                "spring.data.redis.lettuce.pool.max-active=300")
            .run(context -> {
                assertNull(context.getStartupFailure());
                assertFalse(context.containsBean("springSessionRedisConnectionFactory"));
                assertTrue(context.containsBean("redisConnectionFactory"));

                var ticketConnectionFactory = context.getBean(
                    "redisTicketConnectionFactory", RedisConnectionFactory.class);
                var sessionConnectionFactory = context.getBean(
                    "redisConnectionFactory", LettuceConnectionFactory.class);
                assertNotSame(ticketConnectionFactory, sessionConnectionFactory);
                assertEquals("session-redis.example.org", sessionConnectionFactory.getHostName());
                assertEquals(6380, sessionConnectionFactory.getPort());

                var clientConfiguration = assertInstanceOf(
                    LettucePoolingClientConfiguration.class,
                    sessionConnectionFactory.getClientConfiguration());
                GenericObjectPoolConfig<StatefulConnection<?, ?>> pool = clientConfiguration.getPoolConfig();
                assertEquals(300, pool.getMaxTotal());

                var repository = context.getBean(RedisSessionRepository.class);
                var redisTemplate = (RedisTemplate<String, Object>) repository.getSessionRedisOperations();
                assertSame(sessionConnectionFactory, redisTemplate.getConnectionFactory());
            });
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class TicketRedisConfiguration {
        @Bean(defaultCandidate = false)
        RedisConnectionFactory redisTicketConnectionFactory() {
            return mock(RedisConnectionFactory.class);
        }
    }
}
