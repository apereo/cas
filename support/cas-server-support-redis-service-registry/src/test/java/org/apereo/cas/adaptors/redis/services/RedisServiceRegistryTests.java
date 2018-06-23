package org.apereo.cas.adaptors.redis.services;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.config.RedisServiceRegistryConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.AbstractServiceRegistryTests;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServiceRegistry;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.TestPropertySource;
import redis.embedded.RedisServer;

import java.util.Arrays;
import java.util.Collection;

/**
 * Unit test for {@link RedisServiceRegistry} class.
 *
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@RunWith(Parameterized.class)
@SpringBootTest(classes = {RedisServiceRegistryConfiguration.class, RefreshAutoConfiguration.class})
@EnableScheduling
@TestPropertySource(locations = {"classpath:/svc-redis.properties"})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class RedisServiceRegistryTests extends AbstractServiceRegistryTests {
    private static RedisServer REDIS_SERVER;

    @Autowired
    @Qualifier("redisServiceRegistry")
    private ServiceRegistry dao;

    public RedisServiceRegistryTests(final Class<? extends RegisteredService> registeredServiceClass) {
        super(registeredServiceClass);
    }

    @Override
    public ServiceRegistry getNewServiceRegistry() {
        return this.dao;
    }

    @BeforeClass
    public static void startRedis() throws Exception {
        REDIS_SERVER = new RedisServer(6380);
        REDIS_SERVER.start();
    }

    @AfterClass
    public static void stopRedis() {
        REDIS_SERVER.stop();
    }

    @Parameterized.Parameters
    public static Collection<Object> getTestParameters() {
        return Arrays.asList(RegexRegisteredService.class);
    }
}
