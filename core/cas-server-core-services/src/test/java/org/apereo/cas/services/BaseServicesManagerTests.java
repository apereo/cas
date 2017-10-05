package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Service;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link BaseServicesManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public abstract class BaseServicesManagerTests {
    private static final String TEST = "test";
    protected ServiceRegistryDao serviceRegistryDao;
    protected ServicesManager servicesManager;
    protected final List<RegisteredService> listOfDefaultServices = new ArrayList<>();

    public BaseServicesManagerTests() {

        final RegexRegisteredService r = new RegexRegisteredService();
        r.setId(2500);
        r.setServiceId("serviceId");
        r.setName("serviceName");
        r.setEvaluationOrder(1000);
        listOfDefaultServices.clear();
        listOfDefaultServices.add(r);
    }

    @Before
    public void setUp() throws Exception {
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

    @Test
    public void verifyMultiServicesBySameNameAndServiceId() {
        RegexRegisteredService r = new RegexRegisteredService();
        r.setId(666);
        r.setName("testServiceName");
        r.setServiceId("testServiceA");

        this.servicesManager.save(r);

        r = new RegexRegisteredService();
        r.setId(999);
        r.setName("testServiceName");
        r.setServiceId("testServiceA");

        this.servicesManager.save(r);

        /* Added 2 above, plus another that is added during @Setup */
        assertEquals(3, this.servicesManager.getAllServices().size());
    }

    @Test
    public void verifySaveWithReturnedPersistedInstance() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setId(1000L);
        r.setName(TEST);
        r.setServiceId(TEST);

        final RegisteredService persistedRs = this.servicesManager.save(r);
        assertNotNull(persistedRs);
        assertEquals(1000L, persistedRs.getId());
    }

    @Test
    public void verifyDeleteAndGet() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setId(1000);
        r.setName(TEST);
        r.setServiceId(TEST);

        this.servicesManager.save(r);
        assertEquals(r, this.servicesManager.findServiceBy(r.getId()));

        this.servicesManager.delete(r.getId());
        assertNull(this.servicesManager.findServiceBy(r.getId()));
    }

    @Test
    public void verifyDeleteNotExistentService() {
        assertNull(this.servicesManager.delete(1500));
    }

    @Test
    public void verifyMatchesExistingService() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setId(1000);
        r.setName(TEST);
        r.setServiceId(TEST);

        final Service service = RegisteredServiceTestUtils.getService(TEST);
        final Service service2 = RegisteredServiceTestUtils.getService("fdfa");

        this.servicesManager.save(r);

        assertTrue(this.servicesManager.matchesExistingService(service));
        assertEquals(r, this.servicesManager.findServiceBy(service));
        assertNull(this.servicesManager.findServiceBy(service2));
    }

    @Test
    public void verifyAllService() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setId(1000);
        r.setName(TEST);
        r.setServiceId(TEST);
        r.setEvaluationOrder(2);

        this.servicesManager.save(r);

        assertEquals(2, this.servicesManager.getAllServices().size());
        assertTrue(this.servicesManager.getAllServices().contains(r));
    }

    @Test
    public void verifyRegexService() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setId(10000);
        r.setName("regex test");
        r.setServiceId("^http://www.test.edu/.+");
        r.setEvaluationOrder(10000);

        this.servicesManager.save(r);

        final Service service = RegisteredServiceTestUtils.getService("HTTP://www.TEST.edu/param=hello");
        assertEquals(r, this.servicesManager.findServiceBy(service));
    }

    @Test
    public void verifyEmptyServicesRegistry() {
        final Service s = RegisteredServiceTestUtils.getService("http://www.google.com");

        servicesManager.getAllServices().forEach(svc -> servicesManager.delete(svc.getId()));

        assertSame(0, this.servicesManager.getAllServices().size());
        assertNull(this.servicesManager.findServiceBy(s));
        assertNull(this.servicesManager.findServiceBy(1000));
    }

    @Test
    public void verifyEvaluationOrderOfServices() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setId(100);
        r.setName(TEST);
        r.setServiceId(TEST);
        r.setEvaluationOrder(200);

        final RegexRegisteredService r2 = new RegexRegisteredService();
        r2.setId(101);
        r2.setName(TEST);
        r2.setServiceId(TEST);
        r2.setEvaluationOrder(80);

        final RegexRegisteredService r3 = new RegexRegisteredService();
        r3.setId(102);
        r3.setName("Sample test service");
        r3.setServiceId(TEST);
        r3.setEvaluationOrder(80);

        this.servicesManager.save(r);
        this.servicesManager.save(r3);
        this.servicesManager.save(r2);

        final List<RegisteredService> allServices = new ArrayList<>(this.servicesManager.getAllServices());

        //We expect the 3 newly added services, plus the one added in setUp()
        assertEquals(4, allServices.size());

        assertEquals(allServices.get(0).getId(), r3.getId());
        assertEquals(allServices.get(1).getId(), r2.getId());
        assertEquals(allServices.get(2).getId(), r.getId());
    }

    @Test
    public void verifyServiceCanBeUpdated() throws Exception {
        final int serviceId = 2500;
        final String description = "desc";

        final RegexRegisteredService service = new RegexRegisteredService();
        service.setId(serviceId);
        service.setName("serviceName");
        service.setServiceId("serviceId");
        service.setEvaluationOrder(1000);

        servicesManager.save(service);

        service.setDescription(description);

        servicesManager.save(service);

        final Collection<RegisteredService> serviceRetrieved = servicesManager.findServiceBy(RegexRegisteredService.class::isInstance);

        assertEquals(description, serviceRetrieved.toArray(new RegisteredService[]{})[0].getDescription());
    }

    @Test
    public void verifyServiceIsUpdatedAfterALoad() throws Exception {
        final int serviceId = 2500;
        final String description = "desc";

        final RegexRegisteredService service = new RegexRegisteredService();
        service.setId(serviceId);
        service.setName("serviceName");
        service.setServiceId("serviceId");
        service.setEvaluationOrder(1000);
        serviceRegistryDao.save(service);
        service.setDescription(description);

        serviceRegistryDao.save(service);
        servicesManager.load();

        final Collection<RegisteredService> serviceRetrieved = servicesManager.findServiceBy(RegexRegisteredService.class::isInstance);

        assertEquals(description, serviceRetrieved.toArray(new RegisteredService[]{})[0].getDescription());
    }

    @Test
    public void verifyExpiredServiceDisabled() {
        final RegexRegisteredService service = new RegexRegisteredService();
        service.setServiceId("newExpiredService");
        service.setExpirationPolicy(new DefaultRegisteredServiceExpirationPolicy(false, LocalDate.now()));
        servicesManager.save(service);

        assertSame(1, this.servicesManager.getAllServices().size());
        final RegisteredService svc = this.servicesManager.findServiceBy("newExpiredService");
        assertNotNull(svc);
        assertFalse(svc.getAccessStrategy().isServiceAccessAllowed());
    }

    @Test
    public void verifyExpiredServiceDeleted() {
        final RegexRegisteredService service = new RegexRegisteredService();
        service.setServiceId("newExpiredAndDeletedService");
        service.setExpirationPolicy(new DefaultRegisteredServiceExpirationPolicy(LocalDate.now()));
        servicesManager.save(service);

        assertSame(1, this.servicesManager.getAllServices().size());
        final RegisteredService svc = this.servicesManager.findServiceBy("newExpiredAndDeletedService");
        assertNull(svc);
    }
}
