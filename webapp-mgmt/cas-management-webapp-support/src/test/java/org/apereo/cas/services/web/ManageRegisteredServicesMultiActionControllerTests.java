package org.apereo.cas.services.web;

import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.mgmt.services.web.ManageRegisteredServicesMultiActionController;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceEditBean.ServiceData;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceViewBean;
import org.apereo.cas.mgmt.services.web.factory.DefaultAccessStrategyMapper;
import org.apereo.cas.mgmt.services.web.factory.DefaultAttributeFilterMapper;
import org.apereo.cas.mgmt.services.web.factory.DefaultAttributeReleasePolicyMapper;
import org.apereo.cas.mgmt.services.web.factory.DefaultPrincipalAttributesRepositoryMapper;
import org.apereo.cas.mgmt.services.web.factory.DefaultProxyPolicyMapper;
import org.apereo.cas.mgmt.services.web.factory.DefaultRegisteredServiceFactory;
import org.apereo.cas.mgmt.services.web.factory.DefaultRegisteredServiceMapper;
import org.apereo.cas.mgmt.services.web.factory.DefaultUsernameAttributeProviderMapper;
import org.apereo.cas.mgmt.services.web.factory.RegisteredServiceMapper;
import org.apereo.cas.services.DefaultServicesManager;
import org.apereo.cas.services.InMemoryServiceRegistry;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
    private DefaultServicesManager servicesManager;
    private final DefaultAttributeReleasePolicyMapper policyMapper =
            new DefaultAttributeReleasePolicyMapper(new DefaultAttributeFilterMapper(),
                    new DefaultPrincipalAttributesRepositoryMapper(),
                    new ArrayList<>());

    @Before
    public void setUp() throws Exception {
        this.servicesManager = new DefaultServicesManager(new InMemoryServiceRegistry());

        this.registeredServiceFactory = new DefaultRegisteredServiceFactory(new DefaultAccessStrategyMapper(), policyMapper, new DefaultProxyPolicyMapper(),
                new DefaultRegisteredServiceMapper(), new DefaultUsernameAttributeProviderMapper(), Collections.emptyList());

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
        assertTrue(response.getContentAsString().contains("serviceName"));
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
        this.thrown.expectMessage("Service id 5000 cannot be found.");

        this.servicesManager.save(r);
        this.controller.updateRegisteredServiceEvaluationOrder(new MockHttpServletResponse(), 5000, 1000);
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

        this.controller.getServices(response);
        final String content = response.getContentAsString();
        assertTrue(content.contains(SERVICES));
        assertTrue(content.contains(UNIQUE_DESCRIPTION));
    }

    @Test
    public void verifyCustomComponents() throws Exception {
        // override the RegisteredServiceMapper
        this.registeredServiceFactory = new DefaultRegisteredServiceFactory(new DefaultAccessStrategyMapper(), policyMapper, new DefaultProxyPolicyMapper(),
                new CustomRegisteredServiceMapper(), new DefaultUsernameAttributeProviderMapper(), Collections.emptyList());

        this.controller = new ManageRegisteredServicesMultiActionController(this.servicesManager, this
                .registeredServiceFactory, new WebApplicationServiceFactory(), "https://cas.example.org");

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

        this.controller.getServices(response);
        final String content = response.getContentAsString();
        assertTrue(content.contains(SERVICES));
        assertTrue(content.contains(UNIQUE_DESCRIPTION));
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
            final Map<String, Object> properties = new HashMap<>();
            properties.put("key1", "string");
            properties.put("key2", 100);
            bean.setCustomComponent("customComponent1", properties);
        }

        @Override
        public RegisteredService toRegisteredService(final ServiceData data) {
            return base.toRegisteredService(data);
        }
    }
}
