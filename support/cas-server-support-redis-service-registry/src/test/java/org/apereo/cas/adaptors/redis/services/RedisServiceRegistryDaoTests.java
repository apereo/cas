package org.apereo.cas.adaptors.redis.services;

import org.apereo.cas.config.RedisServiceRegistryConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServiceRegistryDao;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import redis.embedded.RedisServer;

import java.util.List;

/**
 * Unit test for {@link RedisServiceRegistryDao} class.
 *
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RedisServiceRegistryConfiguration.class, RefreshAutoConfiguration.class})
@EnableScheduling
@TestPropertySource(locations={"classpath:/svc-redis.properties"})
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class RedisServiceRegistryDaoTests {
    private static RedisServer REDIS_SERVER;
    
    @Autowired
    @Qualifier("serviceRegistryDao")
    private ServiceRegistryDao dao;

    @Before
    public void setUp() {
        final List<RegisteredService> services = this.dao.load();
        services.forEach(service -> this.dao.delete(service));
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
    
    @Test
    public void verify() {
        
    }
}
