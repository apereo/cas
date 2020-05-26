package org.apereo.cas.services;

import org.apereo.cas.JmsQueueIdentifier;
import org.apereo.cas.configuration.model.support.hazelcast.BaseHazelcastProperties;
import org.apereo.cas.hz.HazelcastConfigurationFactory;
import org.apereo.cas.services.publisher.CasRegisteredServiceHazelcastStreamPublisher;
import org.apereo.cas.support.events.service.CasRegisteredServiceDeletedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServiceLoadedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServiceSavedEvent;
import org.apereo.cas.util.cache.DistributedCacheObject;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RegisteredServiceHazelcastDistributedCacheManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Hazelcast")
public class RegisteredServiceHazelcastDistributedCacheManagerTests {
    private HazelcastInstance hz;
    private RegisteredServiceHazelcastDistributedCacheManager mgr;

    @BeforeEach
    public void initialize() {
        val properties = new BaseHazelcastProperties();
        properties.getCluster().setInstanceName(getClass().getSimpleName());
        val config = HazelcastConfigurationFactory.build(properties,
            HazelcastConfigurationFactory.buildMapConfig(properties, "cache", 10));
        this.hz = Hazelcast.newHazelcastInstance(config);
        mgr = new RegisteredServiceHazelcastDistributedCacheManager(this.hz);
    }

    @AfterEach
    public void shutdown() {
        this.hz.shutdown();
    }

    @Test
    public void verifyAction() {

        val registeredService = RegisteredServiceTestUtils.getRegisteredService();
        var obj = mgr.get(registeredService);
        assertNull(obj);
        assertFalse(mgr.contains(registeredService));

        val cache = new DistributedCacheObject<RegisteredService>(registeredService);
        mgr.set(registeredService, cache);
        assertFalse(mgr.getAll().isEmpty());
        obj = mgr.get(registeredService);
        assertNotNull(obj);
        val c = mgr.findAll(obj1 -> obj1.getValue().equals(registeredService));
        assertFalse(c.isEmpty());
        mgr.remove(registeredService, cache);
        assertTrue(mgr.getAll().isEmpty());
    }

    @Test
    public void verifyPublisher() {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService();
        val publisher = new CasRegisteredServiceHazelcastStreamPublisher(mgr, new JmsQueueIdentifier("123456"));
        publisher.publish(registeredService, new CasRegisteredServiceDeletedEvent(this, registeredService));
        publisher.publish(registeredService, new CasRegisteredServiceSavedEvent(this, registeredService));
        publisher.publish(registeredService, new CasRegisteredServiceLoadedEvent(this, registeredService));
        assertFalse(mgr.getAll().isEmpty());
    }
}
