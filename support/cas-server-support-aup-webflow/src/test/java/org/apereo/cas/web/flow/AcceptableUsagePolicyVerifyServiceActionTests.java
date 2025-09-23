package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.DefaultRegisteredServiceAcceptableUsagePolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.webflow.execution.Action;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AcceptableUsagePolicyVerifyServiceActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("WebflowServiceActions")
class AcceptableUsagePolicyVerifyServiceActionTests extends BaseAcceptableUsagePolicyActionTests {
    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_AUP_VERIFY_SERVICE)
    private Action acceptableUsagePolicyVerifyAction;

    @Test
    void verifyNoService() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        assertNull(acceptableUsagePolicyVerifyAction.execute(context));
    }

    @Test
    void verifyDisabledService() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

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
    void verifyMustAccept() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

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
