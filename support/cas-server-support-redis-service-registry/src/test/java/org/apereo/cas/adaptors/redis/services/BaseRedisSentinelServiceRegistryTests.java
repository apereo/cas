package org.apereo.cas.adaptors.redis.services;

import org.apereo.cas.config.CasRedisServiceRegistryAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.AbstractServiceRegistryTests;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.test.CasTestExtension;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableScheduling;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Common class of Unit test for {@link RedisServiceRegistry} class.
 *
 * @author Julien Gribonvald
 * @since 6.1.0
 */
@SpringBootTest(classes = {
    CasRedisServiceRegistryAutoConfiguration.class,
    AbstractServiceRegistryTests.SharedTestConfiguration.class
})
@EnableScheduling
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Getter
@ExtendWith(CasTestExtension.class)
public abstract class BaseRedisSentinelServiceRegistryTests extends AbstractServiceRegistryTests {
    @Autowired
    @Qualifier("redisServiceRegistry")
    private ServiceRegistry newServiceRegistry;

    @Test
    void verifyFailures() {
        assertNull(newServiceRegistry.save((RegisteredService) null));
        assertFalse(newServiceRegistry.delete(null));
    }
}
