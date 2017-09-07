package org.apereo.cas.services.web;

import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.mgmt.services.web.ManageRegisteredServicesMultiActionController;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceViewBean;
import org.apereo.cas.mgmt.services.web.factory.DefaultRegisteredServiceFactory;
import org.apereo.cas.services.DomainServicesManager;
import org.apereo.cas.services.InMemoryServiceRegistry;
import org.apereo.cas.services.RegexRegisteredService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.1
 */
@RunWith(JUnit4.class)
public class ManageRegisteredServicesMultiActionControllerTests {

    private static final String NAME = "name";
    private static final String UNIQUE_DESCRIPTION = "uniqueDescription";
    private static final String SERVICES = "services";
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private ManageRegisteredServicesMultiActionController controller;
    private DefaultRegisteredServiceFactory registeredServiceFactory;
    private DomainServicesManager servicesManager;

    @Before
    public void setUp() throws Exception {
        this.servicesManager = new DomainServicesManager(new InMemoryServiceRegistry());

        this.registeredServiceFactory = new DefaultRegisteredServiceFactory(new ArrayList<>(0));

        this.controller = new ManageRegisteredServicesMultiActionController(this.servicesManager, this
                .registeredServiceFactory, new WebApplicationServiceFactory(), "https://cas.example.org");
    }

    @Test
    public void verifyDeleteService() throws Exception {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setId(1200);
        r.setName(NAME);
        r.setServiceId("serviceId");
        r.setEvaluationOrder(1);

        this.servicesManager.save(r);

        final MockHttpServletResponse response = new MockHttpServletResponse();
        this.controller.manage(response);
        this.controller.deleteRegisteredService(1200, response);

        assertNull(this.servicesManager.findServiceBy(1200));
    }

    @Test
    public void verifyDeleteServiceNoService() throws Exception {
        final MockHttpServletResponse response = new MockHttpServletResponse();

        this.thrown.expect(IllegalArgumentException.class);
        this.thrown.expectMessage("The default service https://cas.example.org cannot be deleted. The definition is required for accessing the application.");

        this.controller.deleteRegisteredService(1200, response);
        assertNull(this.servicesManager.findServiceBy(1200));
        assertFalse(response.getContentAsString().contains("serviceName"));
    }

    @Test
    public void updateEvaluationOrderInvalidServiceId() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setId(1200);
        r.setName(NAME);
        r.setServiceId("test");
        r.setEvaluationOrder(2);

        this.thrown.expect(IllegalArgumentException.class);

        this.servicesManager.save(r);
        final RegisteredServiceViewBean[] svcs = new RegisteredServiceViewBean[2];
        RegisteredServiceViewBean rsb = new RegisteredServiceViewBean();
        rsb.setAssignedId("5000");
        svcs[0] = rsb;
        rsb = new RegisteredServiceViewBean();
        rsb.setAssignedId("1200");
        svcs[1] = rsb;
        this.controller.updateOrder(new MockHttpServletRequest(), new MockHttpServletResponse(), svcs);
    }

    @Test
    public void verifyManage() throws Exception {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setId(1200);
        r.setName(NAME);
        r.setDescription(UNIQUE_DESCRIPTION);
        r.setServiceId("test");
        r.setEvaluationOrder(2);

        this.servicesManager.save(r);

        final MockHttpServletResponse response = new MockHttpServletResponse();
        final ModelAndView mv = this.controller.manage(response);

        assertTrue(mv.getModel().containsKey("defaultServiceUrl"));
        assertTrue(mv.getModel().containsKey("status"));
    }
}
