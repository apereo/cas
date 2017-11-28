package org.apereo.cas.adaptors.redis.services;

import org.apereo.cas.config.RedisServiceRegistryConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.DefaultRegisteredServiceUsernameProvider;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;
import org.apereo.cas.services.ServiceRegistryDao;
import org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy;
import org.apereo.cas.util.CollectionUtils;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

/**
 * Unit test for {@link RedisServiceRegistryDao} class.
 *
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RedisServiceRegistryConfiguration.class, RefreshAutoConfiguration.class})
@EnableScheduling
@TestPropertySource(locations = {"classpath:/svc-redis.properties"})
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
    public void execSaveMethodWithDefaultUsernameAttribute() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setName("execSaveMethodWithDefaultUsernameAttribute");
        r.setServiceId("testing");
        r.setDescription("New service");
        r.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        final ReturnAllAttributeReleasePolicy policy = new ReturnAllAttributeReleasePolicy();
        policy.setConsentPolicy(new DefaultRegisteredServiceConsentPolicy(CollectionUtils.wrapSet("test"),
                CollectionUtils.wrapSet("test")));
        r.setAttributeReleasePolicy(policy);
        final RegisteredService r2 = this.dao.save(r);
        assertEquals(r2, r);
    }

    @Test
    public void verifyServiceRemovals() {
        final List<RegisteredService> list = new ArrayList<>(5);
        IntStream.range(1, 3).forEach(i -> {
            final RegexRegisteredService r = new RegexRegisteredService();
            r.setServiceId("serviceId" + i);
            r.setName("testServiceType");
            r.setTheme("testtheme");
            r.setEvaluationOrder(1000);
            r.setId(i * 100);
            list.add(this.dao.save(r));
        });
        this.dao.load().forEach(r2 -> {
            this.dao.delete(r2);
            assertNull(this.dao.findServiceById(r2.getId()));
        });
    }

}
