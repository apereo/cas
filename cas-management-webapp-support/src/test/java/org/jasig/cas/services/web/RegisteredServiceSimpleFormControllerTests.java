package org.jasig.cas.services.web;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.jasig.cas.services.AbstractRegisteredService;
import org.jasig.cas.services.DefaultServicesManagerImpl;
import org.jasig.cas.services.InMemoryServiceRegistryDaoImpl;
import org.jasig.cas.services.RegexRegisteredService;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceImpl;
import org.jasig.cas.services.web.beans.RegisteredServiceEditBean;
import org.jasig.cas.services.web.beans.RegisteredServiceViewBean;
import org.jasig.cas.services.web.factory.AttributeFormDataPopulator;
import org.jasig.cas.services.web.factory.DefaultRegisteredServiceFactory;
import org.jasig.cas.services.web.factory.DefaultRegisteredServiceMapper;
import org.jasig.cas.services.web.factory.RegisteredServiceMapper;
import org.jasig.services.persondir.support.StubPersonAttributeDao;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ModelMap;
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
 * @author Scott Battaglia
 * @author Misagh Moayyed
 * @since 3.1
 */
@RunWith(JUnit4.class)
public class RegisteredServiceSimpleFormControllerTests {

    private RegisteredServiceSimpleFormController controller;

    private DefaultServicesManagerImpl manager;

    private StubPersonAttributeDao repository;

    private DefaultRegisteredServiceFactory registeredServiceFactory;

    @Before
    public void setUp() throws Exception {
        final Map<String, List<Object>> attributes = new HashMap<>();
        attributes.put("test", Arrays.asList(new Object[] {"test"}));

        this.repository = new StubPersonAttributeDao();
        this.repository.setBackingMap(attributes);

        this.registeredServiceFactory = new DefaultRegisteredServiceFactory();
        this.registeredServiceFactory.setFormDataPopulators(ImmutableList.of(new AttributeFormDataPopulator(this
                .repository)));
        this.registeredServiceFactory.initializeDefaults();

        this.manager = new DefaultServicesManagerImpl(
                new InMemoryServiceRegistryDaoImpl());
        this.manager.setApplicationEventPublisher(mock(ApplicationEventPublisher.class));
        this.controller = new RegisteredServiceSimpleFormController(this.manager, this.registeredServiceFactory);
    }

    @Test
    public void verifyAddRegisteredServiceNoValues() throws Exception {
        final BindingResult result = mock(BindingResult.class);
        when(result.getModel()).thenReturn(new HashMap<String, Object>());
        when(result.hasErrors()).thenReturn(true);
        
        final ModelMap model = new ModelMap();
        //this.controller.onSubmit(mock(RegisteredService.class), result, model, new MockHttpServletRequest());
        
        assertTrue(result.hasErrors());
    }

    @Test
    public void verifyAddRegisteredServiceWithValues() throws Exception {
        final RegisteredServiceImpl svc = new RegisteredServiceImpl();
        svc.setDescription("description");
        svc.setServiceId("serviceId");
        svc.setName("name");
        svc.setEvaluationOrder(123);
        
        assertTrue(this.manager.getAllServices().isEmpty());
        final RegisteredServiceEditBean.ServiceData data = registeredServiceFactory.createServiceData(svc);
        this.controller.saveService(new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                data, mock(BindingResult.class));

        final Collection<RegisteredService> services = this.manager.getAllServices();
        assertEquals(1, services.size());
        for(final RegisteredService rs : this.manager.getAllServices()) {
            assertTrue(rs instanceof RegexRegisteredService);
        }
    }

    @Test
    public void verifyEditRegisteredServiceWithValues() throws Exception {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setId(1000);
        r.setName("Test Service");
        r.setServiceId("test");
        r.setDescription("description");

        this.manager.save(r);

        final RegisteredServiceImpl svc = new RegisteredServiceImpl();
        svc.setDescription("description");
        svc.setServiceId("serviceId1");
        svc.setName("name");
        svc.setId(1000);
        svc.setEvaluationOrder(1000);

        final RegisteredServiceEditBean.ServiceData data = registeredServiceFactory.createServiceData(svc);
        this.controller.saveService(new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                data, mock(BindingResult.class));

        assertFalse(this.manager.getAllServices().isEmpty());
        final RegisteredService r2 = this.manager.findServiceBy(1000);

        assertEquals("serviceId1", r2.getServiceId());
    }

   @Test
    public void verifyAddRegexRegisteredService() throws Exception {
        final RegexRegisteredService svc = new RegexRegisteredService();
        svc.setDescription("description");
        svc.setServiceId("^serviceId");
        svc.setName("name");
        svc.setId(1000);
        svc.setEvaluationOrder(1000);

       final RegisteredServiceEditBean.ServiceData data = registeredServiceFactory.createServiceData(svc);
       this.controller.saveService(new MockHttpServletRequest(),
               new MockHttpServletResponse(),
               data, mock(BindingResult.class));

        final Collection<RegisteredService> services = this.manager.getAllServices();
        assertEquals(1, services.size());
        for(final RegisteredService rs : this.manager.getAllServices()) {
            assertTrue(rs instanceof RegexRegisteredService);
        }
    }

    @Test
    public void verifyAddMultipleRegisteredServiceTypes() throws Exception {
        AbstractRegisteredService svc = new RegexRegisteredService();
        svc.setDescription("description");
        svc.setServiceId("^serviceId");
        svc.setName("name");
        svc.setId(1000);
        svc.setEvaluationOrder(1000);

        final RegisteredServiceEditBean.ServiceData data = registeredServiceFactory.createServiceData(svc);
        this.controller.saveService(new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                data, mock(BindingResult.class));

        svc = new RegisteredServiceImpl();
        svc.setDescription("description");
        svc.setServiceId("^serviceId");
        svc.setName("name");
        svc.setId(100);
        svc.setEvaluationOrder(100);

        final RegisteredServiceEditBean.ServiceData data2 = registeredServiceFactory.createServiceData(svc);
        this.controller.saveService(new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                data2, mock(BindingResult.class));

        final Collection<RegisteredService> services = this.manager.getAllServices();
        assertEquals(2, services.size());
    }

    @Test
    public void verifyAddMockRegisteredService() throws Exception {
        registeredServiceFactory.setRegisteredServiceMapper(new MockRegisteredServiceMapper());

        final MockRegisteredService svc = new MockRegisteredService();
        svc.setDescription("description");
        svc.setServiceId("^serviceId");
        svc.setName("name");
        svc.setId(1000);
        svc.setEvaluationOrder(1000);

        final RegisteredServiceEditBean.ServiceData data = registeredServiceFactory.createServiceData(svc);
        this.controller.saveService(new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                data, mock(BindingResult.class));

        final Collection<RegisteredService> services = this.manager.getAllServices();
        assertEquals(1, services.size());
        for (final  RegisteredService rs : this.manager.getAllServices()) {
            assertTrue(rs instanceof MockRegisteredService);
        }
    }

    
    @Test
    public void verifyEditMockRegisteredService() throws Exception {
        registeredServiceFactory.setRegisteredServiceMapper(new MockRegisteredServiceMapper());

        final MockRegisteredService r = new MockRegisteredService();
        r.setId(1000);
        r.setName("Test Service");
        r.setServiceId("test");
        r.setDescription("description");

        this.manager.save(r);
        
        r.setServiceId("serviceId1");
        final RegisteredServiceEditBean.ServiceData data = registeredServiceFactory.createServiceData(r);
        this.controller.saveService(new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                data, mock(BindingResult.class));

        assertFalse(this.manager.getAllServices().isEmpty());
        final RegisteredService r2 = this.manager.findServiceBy(1000);

        assertEquals("serviceId1", r2.getServiceId());
        assertTrue(r2 instanceof MockRegisteredService);
    }

    private static class MockRegisteredService extends RegexRegisteredService {
        private static final long serialVersionUID = -7746061989010390744L;

        @Override
        protected AbstractRegisteredService newInstance() {
            return new MockRegisteredService();
        }
    }

    private static class MockRegisteredServiceMapper implements RegisteredServiceMapper {
        private final RegisteredServiceMapper base = new DefaultRegisteredServiceMapper();

        @Override
        public void mapRegisteredService(final RegisteredService svc,
                                         final RegisteredServiceEditBean.ServiceData bean) {
            base.mapRegisteredService(svc, bean);
            if (svc instanceof MockRegisteredService) {
                bean.setCustomComponent("mock", ImmutableMap.of("service_type", "MockRegisteredService"));
            }
        }

        @Override
        public void mapRegisteredService(final RegisteredService svc, final RegisteredServiceViewBean bean) {
            base.mapRegisteredService(svc, bean);
        }

        @Override
        public RegisteredService toRegisteredService(final RegisteredServiceEditBean.ServiceData data) {
            final RegisteredService baseSvc = base.toRegisteredService(data);

            // return base svc if this isn't a MockRegisteredService
            final Map<String, ?> mockComponent = data.getCustomComponent("mock");
            if (mockComponent == null || !"MockRegisteredService".equals(mockComponent.get("service_type"))) {
                return baseSvc;
            }

            // copy data from baseSvc to MockRegisteredService
            final MockRegisteredService svc = new MockRegisteredService();
            svc.copyFrom(baseSvc);
            return svc;
        }
    }
}
