package org.jasig.cas.services.web;

import org.jasig.cas.services.DefaultServicesManagerImpl;
import org.jasig.cas.services.InMemoryServiceRegistryDaoImpl;
import org.jasig.cas.services.RegisteredServiceImpl;
import org.jasig.cas.services.web.factory.DefaultRegisteredServiceFactory;
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
        r.setServiceId("test");
        r.setEvaluationOrder(2);

        this.servicesManager.save(r);

        final MockHttpServletResponse response = new MockHttpServletResponse();
        final ModelAndView mv = this.controller.manage(response);

        assertTrue(mv.getModel().containsKey("defaultServiceUrl"));
        assertTrue(mv.getModel().containsKey("status"));

        this.controller.getServices(response);
        assertTrue(response.getContentAsString().contains("services"));
    }
}
