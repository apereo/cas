package org.apereo.cas.services;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.apereo.cas.DistributedCacheObject;
import org.apereo.cas.StringBean;
import org.apereo.cas.configuration.model.support.hazelcast.BaseHazelcastProperties;
import org.apereo.cas.hz.HazelcastConfigurationFactory;
import org.apereo.cas.services.publisher.CasRegisteredServiceHazelcastStreamPublisher;
import org.apereo.cas.support.events.service.CasRegisteredServiceDeletedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServiceLoadedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServiceSavedEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.*;

/**
 * This is {@link RegisteredServiceHazelcastDistributedCacheManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class RegisteredServiceHazelcastDistributedCacheManagerTests {
    private HazelcastInstance hz;
    private RegisteredServiceHazelcastDistributedCacheManager mgr;

    @Before
    public void setup() {
        final HazelcastConfigurationFactory factory = new HazelcastConfigurationFactory();
        final BaseHazelcastProperties properties = new BaseHazelcastProperties();
        properties.getCluster().setInstanceName(getClass().getSimpleName());
        final Config config = factory.build(properties, factory.buildMapConfig(properties, "cache", 10));
        this.hz = Hazelcast.newHazelcastInstance(config);
        mgr = new RegisteredServiceHazelcastDistributedCacheManager(this.hz);
    }

    @After
    public void shutdown() {
        this.hz.shutdown();
    }

    @Test
    public void verifyAction() {

        final RegisteredService registeredService = RegisteredServiceTestUtils.getRegisteredService();
        DistributedCacheObject obj = mgr.get(registeredService);
        assertNull(obj);
        assertFalse(mgr.contains(registeredService));

        final DistributedCacheObject cache = new DistributedCacheObject(registeredService);
        mgr.set(registeredService, cache);
        assertFalse(mgr.getAll().isEmpty());
        obj = mgr.get(registeredService);
        assertNotNull(obj);
        final Collection c = mgr.findAll(obj1 -> obj1.getValue().equals(registeredService));
        assertFalse(c.isEmpty());
        mgr.remove(registeredService, cache);
        assertTrue(mgr.getAll().isEmpty());
    }

    @Test
    public void verifyPublisher() {
        final RegisteredService registeredService = RegisteredServiceTestUtils.getRegisteredService();
        final CasRegisteredServiceHazelcastStreamPublisher publisher = new CasRegisteredServiceHazelcastStreamPublisher(mgr, new StringBean("123456"));
        publisher.publish(registeredService, new CasRegisteredServiceDeletedEvent(this, registeredService));
        publisher.publish(registeredService, new CasRegisteredServiceSavedEvent(this, registeredService));
        publisher.publish(registeredService, new CasRegisteredServiceLoadedEvent(this, registeredService));
        assertFalse(mgr.getAll().isEmpty());
    }
}
