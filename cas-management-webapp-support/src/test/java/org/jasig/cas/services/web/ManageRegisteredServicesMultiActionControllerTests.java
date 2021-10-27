package org.jasig.cas.services.web;

import com.google.common.collect.ImmutableMap;
import org.jasig.cas.services.DefaultServicesManagerImpl;
import org.jasig.cas.services.InMemoryServiceRegistryDaoImpl;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceImpl;
import org.jasig.cas.services.web.beans.RegisteredServiceEditBean.ServiceData;
import org.jasig.cas.services.web.beans.RegisteredServiceViewBean;
import org.jasig.cas.services.web.factory.DefaultRegisteredServiceFactory;
import org.jasig.cas.services.web.factory.DefaultRegisteredServiceMapper;
import org.jasig.cas.services.web.factory.RegisteredServiceMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Scott Battaglia
 * @since 3.1
 */
@RunWith(JUnit4.class)
public class ManageRegisteredServicesMultiActionControllerTests {

    private ManageRegisteredServicesMultiActionController controller;

    private DefaultRegisteredServiceFactory registeredServiceFactory;

    private DefaultServicesManagerImpl servicesManager;

    @Before
    public void setUp() throws Exception {
        this.servicesManager = new DefaultServicesManagerImpl(new InMemoryServiceRegistryDaoImpl());
        this.servicesManager.setApplicationEventPublisher(mock(ApplicationEventPublisher.class));

        this.registeredServiceFactory = new DefaultRegisteredServiceFactory();
        this.registeredServiceFactory.initializeDefaults();

        this.controller = new ManageRegisteredServicesMultiActionController(this.servicesManager, this
                .registeredServiceFactory, "foo");
    }

    @Test
    public void verifyDeleteService() throws Exception {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setId(1200);
        r.setName("name");
        r.setServiceId("serviceId");
        r.setEvaluationOrder(1);

        this.servicesManager.save(r);

        final MockHttpServletResponse response = new MockHttpServletResponse();
        this.controller.manage(response);
        this.controller.deleteRegisteredService(1200, response);

        assertNull(this.servicesManager.findServiceBy(1200));
        assertTrue(response.getContentAsString().contains("serviceName"));
    }


    @Test(expected = IllegalArgumentException.class)
    public void verifyDeleteServiceNoService() throws Exception {
        final MockHttpServletResponse response = new MockHttpServletResponse();
        this.controller.deleteRegisteredService(1200, response);
        assertNull(this.servicesManager.findServiceBy(1200));
        assertFalse(response.getContentAsString().contains("serviceName"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void updateEvaluationOrderInvalidServiceId() {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setId(1200);
        r.setName("name");
        r.setServiceId("test");
        r.setEvaluationOrder(2);

        this.servicesManager.save(r);
        this.controller.updateRegisteredServiceEvaluationOrder(new MockHttpServletResponse(), 5000, 1000);
    }

    @Test
    public void verifyManage() throws Exception{
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setId(1200);
        r.setName("name");
        r.setDescription("uniqueDescription");
        r.setServiceId("test");
        r.setEvaluationOrder(2);

        this.servicesManager.save(r);

        final MockHttpServletResponse response = new MockHttpServletResponse();
        final ModelAndView mv = this.controller.manage(response);

        assertTrue(mv.getModel().containsKey("defaultServiceUrl"));
        assertTrue(mv.getModel().containsKey("status"));

        this.controller.getServices(response);
        final String content = response.getContentAsString();
        assertTrue(content.contains("services"));
        assertTrue(content.contains("uniqueDescription"));
    }

    @Test
    public void verifyCustomComponents() throws Exception {
        // override the RegisteredServiceMapper
        this.registeredServiceFactory.setRegisteredServiceMapper(new CustomRegisteredServiceMapper());

        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setId(1200);
        r.setName("name");
        r.setDescription("uniqueDescription");
        r.setServiceId("test");
        r.setEvaluationOrder(2);

        this.servicesManager.save(r);

        final MockHttpServletResponse response = new MockHttpServletResponse();
        final ModelAndView mv = this.controller.manage(response);

        assertTrue(mv.getModel().containsKey("defaultServiceUrl"));
        assertTrue(mv.getModel().containsKey("status"));

        this.controller.getServices(response);
        final String content = response.getContentAsString();
        assertTrue(content.contains("services"));
        assertTrue(content.contains("uniqueDescription"));
        assertTrue(content.contains("customComponent1"));
        assertTrue(content.contains("key2"));
    }

    private static class CustomRegisteredServiceMapper implements RegisteredServiceMapper {
        private final RegisteredServiceMapper base = new DefaultRegisteredServiceMapper();

        @Override
        public void mapRegisteredService(final RegisteredService svc, final ServiceData bean) {
            base.mapRegisteredService(svc, bean);
        }

        @Override
        public void mapRegisteredService(final RegisteredService svc, final RegisteredServiceViewBean bean) {
            base.mapRegisteredService(svc, bean);
            bean.setCustomComponent("customComponent1", ImmutableMap.of("key1", "string", "key2", 100));
        }

        @Override
        public RegisteredService toRegisteredService(final ServiceData data) {
            return base.toRegisteredService(data);
        }
    }
}
