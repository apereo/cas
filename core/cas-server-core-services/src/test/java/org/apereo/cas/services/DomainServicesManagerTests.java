package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author battags
 * @since 3.0.0
 */
public class DomainServicesManagerTests {

    private static final String TEST = "test";
    private ServicesManager domainServicesManager;
    private ServiceRegistryDao dao;

    @Before
    public void setUp() throws Exception {
        final List<RegisteredService> list = new ArrayList<>();

        final RegexRegisteredService r = new RegexRegisteredService();
        r.setId(2500);
        r.setServiceId("serviceId");
        r.setName("serviceName");
        r.setEvaluationOrder(1000);

        list.add(r);
        dao = new InMemoryServiceRegistry(list);
        this.domainServicesManager = new DomainServicesManager(dao);
        this.domainServicesManager.load();
    }

    @Test
    public void verifySaveAndGet() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setId(1000);
        r.setName(TEST);
        r.setServiceId(TEST);

        this.domainServicesManager.save(r);
        assertNotNull(this.domainServicesManager.findServiceBy(1000));
    }

    @Test
    public void verifyMultiServicesBySameNameAndServiceId() {
        RegexRegisteredService r = new RegexRegisteredService();
        r.setId(666);
        r.setName("testServiceName");
        r.setServiceId("testServiceA");

        this.domainServicesManager.save(r);

        r = new RegexRegisteredService();
        r.setId(999);
        r.setName("testServiceName");
        r.setServiceId("testServiceA");

        this.domainServicesManager.save(r);

        /* Added 2 above, plus another that is added during @Setup */
        assertEquals(3, this.domainServicesManager.getAllServices().size());
    }

    @Test
    public void verifySaveWithReturnedPersistedInstance() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setId(1000L);
        r.setName(TEST);
        r.setServiceId(TEST);

        final RegisteredService persistedRs = this.domainServicesManager.save(r);
        assertNotNull(persistedRs);
        assertEquals(1000L, persistedRs.getId());
    }

    @Test
    public void verifyDeleteAndGet() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setId(1000);
        r.setName(TEST);
        r.setServiceId(TEST);

        this.domainServicesManager.save(r);
        assertEquals(r, this.domainServicesManager.findServiceBy(r.getId()));

        this.domainServicesManager.delete(r.getId());
        assertNull(this.domainServicesManager.findServiceBy(r.getId()));
    }

    @Test
    public void verifyDeleteNotExistentService() {
        assertNull(this.domainServicesManager.delete(1500));
    }

    @Test
    public void verifyMatchesExistingService() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setId(1000);
        r.setName(TEST);
        r.setServiceId(TEST);

        final Service service = new SimpleService(TEST);
        final Service service2 = new SimpleService("fdfa");

        this.domainServicesManager.save(r);

        assertTrue(this.domainServicesManager.matchesExistingService(service));
        assertEquals(r, this.domainServicesManager.findServiceBy(service));
        assertNull(this.domainServicesManager.findServiceBy(service2));
    }

    @Test
    public void verifyAllService() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setId(1000);
        r.setName(TEST);
        r.setServiceId(TEST);
        r.setEvaluationOrder(2);

        this.domainServicesManager.save(r);

        assertEquals(2, this.domainServicesManager.getAllServices().size());
        assertTrue(this.domainServicesManager.getAllServices().contains(r));
    }

    @Test
    public void verifyRegexService() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setId(10000);
        r.setName("regex test");
        r.setServiceId("^http://www.test.edu.+");
        r.setEvaluationOrder(10000);

        this.domainServicesManager.save(r);

        final SimpleService service = new SimpleService("HTTP://www.TEST.edu/param=hello");
        assertEquals(r, this.domainServicesManager.findServiceBy(service));
    }

    @Test
    public void verifyEmptyServicesRegistry() {
        final SimpleService s = new SimpleService("http://www.google.com");

        domainServicesManager.getAllServices().forEach(svc -> domainServicesManager.delete(svc.getId()));

        assertSame(0, this.domainServicesManager.getAllServices().size());
        assertNull(this.domainServicesManager.findServiceBy(s));
        assertNull(this.domainServicesManager.findServiceBy(1000));
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

        this.domainServicesManager.save(r);
        this.domainServicesManager.save(r3);
        this.domainServicesManager.save(r2);

        final List<RegisteredService> allServices = new ArrayList<>(this.domainServicesManager.getAllServices());

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

        domainServicesManager.save(service);

        service.setDescription(description);

        domainServicesManager.save(service);

        final Collection<RegisteredService> serviceRetrieved = domainServicesManager.findServiceBy(RegexRegisteredService.class::isInstance);

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
        dao.save(service);
        service.setDescription(description);

        dao.save(service);
        domainServicesManager.load();

        final Collection<RegisteredService> serviceRetrieved = domainServicesManager.findServiceBy(RegexRegisteredService.class::isInstance);

        assertEquals(description, serviceRetrieved.toArray(new RegisteredService[]{})[0].getDescription());
    }

    private static class SimpleService implements Service {

        /**
         * Comment for {@code serialVersionUID}.
         */
        private static final long serialVersionUID = 6572142033945243669L;

        private final String id;

        protected SimpleService(final String id) {
            this.id = id;
        }

        @Override
        public Map<String, Object> getAttributes() {
            return null;
        }

        @Override
        public String getId() {
            return this.id;
        }

        @Override
        public void setPrincipal(final Principal principal) {
            // nothing to do
        }

        @Override
        public boolean matches(final Service service) {
            return true;
        }
    }
}
