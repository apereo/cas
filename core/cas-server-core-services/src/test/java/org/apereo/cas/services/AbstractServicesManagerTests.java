package org.apereo.cas.services;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AbstractServicesManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public abstract class AbstractServicesManagerTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractServicesManagerTests.class);

    private static final String TEST = "test";
    protected ServiceRegistryDao serviceRegistryDao;
    protected ServicesManager servicesManager;
    protected final List<RegisteredService> listOfDefaultServices = new ArrayList<>();

    public AbstractServicesManagerTests() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setId(2500);
        r.setServiceId("serviceId");
        r.setName("serviceName");
        r.setEvaluationOrder(1000);
        listOfDefaultServices.add(r);
    }

    @Before
    public void setUp() {
        this.serviceRegistryDao = getServiceRegistryInstance();
        this.servicesManager = getServicesManagerInstance();
        this.servicesManager.load();
    }

    protected ServicesManager getServicesManagerInstance() {
        return new DefaultServicesManager(serviceRegistryDao, mock(ApplicationEventPublisher.class));
    }

    protected ServiceRegistryDao getServiceRegistryInstance() {
        return new InMemoryServiceRegistry(listOfDefaultServices);
    }

    @Test
    public void verifySaveAndGet() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setId(1000);
        r.setName(TEST);
        r.setServiceId(TEST);

        this.servicesManager.save(r);
        assertNotNull(this.servicesManager.findServiceBy(1000));
    }
}
