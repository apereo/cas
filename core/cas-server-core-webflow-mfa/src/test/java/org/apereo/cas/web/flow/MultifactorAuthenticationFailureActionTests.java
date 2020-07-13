package org.apereo.cas.web.flow;

import org.apereo.cas.BaseCasWebflowMultifactorAuthenticationTests;
import org.apereo.cas.authentication.DefaultMultifactorAuthenticationFailureModeEvaluator;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicyFailureModes;
import org.apereo.cas.services.RegisteredServiceTestUtils;
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

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MultifactorAuthenticationFailureActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@DirtiesContext
@Tag("Webflow")
public class MultifactorAuthenticationFailureActionTests extends BaseCasWebflowMultifactorAuthenticationTests {
    @Autowired
    @Qualifier("mfaFailureAction")
    private Action mfaFailureAction;

    protected void executeAction(final RegisteredServiceMultifactorPolicyFailureModes mode, final String transitionId) throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        provider.setFailureMode(mode.name());
        provider.setFailureModeEvaluator(new DefaultMultifactorAuthenticationFailureModeEvaluator(casProperties));

        val service = RegisteredServiceTestUtils.getRegisteredService();
        servicesManager.save(service);
        WebUtils.putRegisteredService(context, service);

        WebUtils.putMultifactorAuthenticationProviderIdIntoFlowScope(context, provider);
        val event = mfaFailureAction.execute(context);
        assertEquals(transitionId, event.getId());
    }

    @Test
    public void verifyOperations() throws Exception {
        executeAction(RegisteredServiceMultifactorPolicyFailureModes.CLOSED, CasWebflowConstants.TRANSITION_ID_UNAVAILABLE);
        executeAction(RegisteredServiceMultifactorPolicyFailureModes.NONE, CasWebflowConstants.TRANSITION_ID_UNAVAILABLE);
        executeAction(RegisteredServiceMultifactorPolicyFailureModes.PHANTOM, CasWebflowConstants.TRANSITION_ID_UNAVAILABLE);
        executeAction(RegisteredServiceMultifactorPolicyFailureModes.UNDEFINED, CasWebflowConstants.TRANSITION_ID_UNAVAILABLE);
    }
}
