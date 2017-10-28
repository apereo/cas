package org.apereo.cas.services.web;

import org.apereo.cas.mgmt.services.web.RegisteredServiceSimpleFormController;
import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.DomainServicesManager;
import org.apereo.cas.services.InMemoryServiceRegistry;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.services.persondir.support.StubPersonAttributeDao;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.validation.BindingResult;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test cases for {@link RegisteredServiceSimpleFormController}.
 *
 * @author Scott Battaglia
 * @author Misagh Moayyed
 * @since 3.1
 */
@RunWith(JUnit4.class)
public class RegisteredServiceSimpleFormControllerTests {

    private static final String NAME = "name";
    private static final String SERVICE_ID = "serviceId";
    private static final String DESCRIPTION = "description";
    private static final String TEST_ID = "test";
    private RegisteredServiceSimpleFormController controller;
    private DomainServicesManager manager;
    private StubPersonAttributeDao repository;

    @Before
    public void setUp() {
        final Map<String, List<Object>> attributes = new HashMap<>();
        attributes.put(TEST_ID, Arrays.asList(new Object[]{TEST_ID}));

        this.repository = new StubPersonAttributeDao();
        this.repository.setBackingMap(attributes);

        this.manager = new DomainServicesManager(new InMemoryServiceRegistry(), mock(ApplicationEventPublisher.class));
        this.controller = new RegisteredServiceSimpleFormController(this.manager);
    }

    @Test
    public void verifyAddRegisteredServiceNoValues() {
        final BindingResult result = mock(BindingResult.class);
        when(result.getModel()).thenReturn(new HashMap<>());
        when(result.hasErrors()).thenReturn(true);
        assertTrue(result.hasErrors());
    }

    @Test
    public void verifyAddRegisteredServiceWithValues() {
        final RegexRegisteredService svc = new RegexRegisteredService();
        svc.setDescription(DESCRIPTION);
        svc.setServiceId(SERVICE_ID);
        svc.setName(NAME);
        svc.setEvaluationOrder(123);

        assertTrue(this.manager.getAllServices().isEmpty());
        this.controller.saveService(svc);

        final Collection<RegisteredService> services = this.manager.getAllServices();
        assertEquals(1, services.size());
        this.manager.getAllServices().forEach(rs -> assertTrue(rs instanceof RegexRegisteredService));
    }

    @Test
    public void verifyEditRegisteredServiceWithValues() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setId(1000);
        r.setName("Test Service");
        r.setServiceId(TEST_ID);
        r.setDescription(DESCRIPTION);

        this.manager.save(r);

        final RegexRegisteredService svc = new RegexRegisteredService();
        svc.setDescription(DESCRIPTION);
        svc.setServiceId("serviceId1");
        svc.setName(NAME);
        svc.setId(1000);
        svc.setEvaluationOrder(1000);

        this.controller.saveService(svc);

        assertFalse(this.manager.getAllServices().isEmpty());
        final RegisteredService r2 = this.manager.findServiceBy(1000);

        assertEquals("serviceId1", r2.getServiceId());
    }

    @Test
    public void verifyAddRegexRegisteredService() {
        final RegexRegisteredService svc = new RegexRegisteredService();
        svc.setDescription(DESCRIPTION);
        svc.setServiceId("^serviceId");
        svc.setName(NAME);
        svc.setId(1000);
        svc.setEvaluationOrder(1000);

        this.controller.saveService(svc);

        final Collection<RegisteredService> services = this.manager.getAllServices();
        assertEquals(1, services.size());
        this.manager.getAllServices().forEach(rs -> assertTrue(rs instanceof RegexRegisteredService));
    }

    @Test
    public void verifyAddMultipleRegisteredServiceTypes() {
        AbstractRegisteredService svc = new RegexRegisteredService();
        svc.setDescription(DESCRIPTION);
        svc.setServiceId("^serviceId");
        svc.setName(NAME);
        svc.setId(1000);
        svc.setEvaluationOrder(1000);

        this.controller.saveService(svc);

        svc = new RegexRegisteredService();
        svc.setDescription(DESCRIPTION);
        svc.setServiceId("^serviceId");
        svc.setName(NAME);
        svc.setId(100);
        svc.setEvaluationOrder(100);

        this.controller.saveService(svc);

        final Collection<RegisteredService> services = this.manager.getAllServices();
        assertEquals(2, services.size());
    }

    @Test
    public void verifyAddMockRegisteredService() {
        this.controller = new RegisteredServiceSimpleFormController(this.manager);

        final RegexRegisteredService svc = new RegexRegisteredService();
        svc.setDescription(DESCRIPTION);
        svc.setServiceId("^serviceId");
        svc.setName(NAME);
        svc.setId(1000);
        svc.setEvaluationOrder(1000);

        this.controller.saveService(svc);

        final Collection<RegisteredService> services = this.manager.getAllServices();
        assertEquals(1, services.size());
        this.manager.getAllServices().forEach(rs -> assertTrue(rs instanceof RegexRegisteredService));
    }

    @Test
    public void verifyEditMockRegisteredService() {
        this.controller = new RegisteredServiceSimpleFormController(this.manager);

        final RegexRegisteredService r = new RegexRegisteredService();
        r.setId(1000);
        r.setName("Test Service");
        r.setServiceId(TEST_ID);
        r.setDescription(DESCRIPTION);

        this.manager.save(r);

        r.setServiceId("serviceId1");
        this.controller.saveService(r);

        assertFalse(this.manager.getAllServices().isEmpty());
        final RegisteredService r2 = this.manager.findServiceBy(1000);

        assertEquals("serviceId1", r2.getServiceId());
        assertTrue(r2 instanceof RegexRegisteredService);
    }
}
