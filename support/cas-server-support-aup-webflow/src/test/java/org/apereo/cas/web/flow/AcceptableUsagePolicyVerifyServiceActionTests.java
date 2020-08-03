package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.DefaultRegisteredServiceAcceptableUsagePolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AcceptableUsagePolicyVerifyServiceActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@DirtiesContext
@Tag("Webflow")
public class AcceptableUsagePolicyVerifyServiceActionTests extends BaseAcceptableUsagePolicyActionTests {
    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("acceptableUsagePolicyVerifyServiceAction")
    private Action acceptableUsagePolicyVerifyAction;

    @Test
    public void verifyNoService() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        assertNull(acceptableUsagePolicyVerifyAction.execute(context));
    }

    @Test
    public void verifyDisabledService() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));

        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
        val service = RegisteredServiceTestUtils.getService("https://aup.service.disabled");
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(service.getId(), Map.of());
        val policy = new DefaultRegisteredServiceAcceptableUsagePolicy();
        policy.setEnabled(false);
        registeredService.setAcceptableUsagePolicy(policy);
        servicesManager.save(registeredService);

        WebUtils.putRegisteredService(context, registeredService);
        WebUtils.putServiceIntoFlowScope(context, service);

        assertNull(acceptableUsagePolicyVerifyAction.execute(context));
    }

    @Test
    public void verifyMustAccept() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));

        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
        val service = RegisteredServiceTestUtils.getService("https://aup.service");
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(service.getId(), Map.of());
        val policy = new DefaultRegisteredServiceAcceptableUsagePolicy();
        policy.setEnabled(true);
        registeredService.setAcceptableUsagePolicy(policy);
        servicesManager.save(registeredService);
        WebUtils.putRegisteredService(context, registeredService);
        WebUtils.putServiceIntoFlowScope(context, service);

        assertEquals(CasWebflowConstants.TRANSITION_ID_AUP_MUST_ACCEPT, acceptableUsagePolicyVerifyAction.execute(context).getId());
    }
}
