package org.apereo.cas.web.flow;

import org.apereo.cas.BaseCasWebflowMultifactorAuthenticationTests;
import org.apereo.cas.authentication.DefaultMultifactorAuthenticationFailureModeEvaluator;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorAuthenticationProviderProperties;
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
@Tag("WebflowMfaActions")
public class MultifactorAuthenticationFailureActionTests extends BaseCasWebflowMultifactorAuthenticationTests {
    @Autowired
    @Qualifier("mfaFailureAction")
    private Action mfaFailureAction;

    @Test
    public void verifyOperations() throws Exception {
        executeAction(BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes.CLOSED, CasWebflowConstants.TRANSITION_ID_UNAVAILABLE);
        executeAction(BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes.NONE, CasWebflowConstants.TRANSITION_ID_UNAVAILABLE);
        executeAction(BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes.PHANTOM, CasWebflowConstants.TRANSITION_ID_UNAVAILABLE);
        executeAction(BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes.UNDEFINED, CasWebflowConstants.TRANSITION_ID_UNAVAILABLE);
    }

    protected void executeAction(final BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes mode, final String transitionId) throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        provider.setFailureMode(mode);
        provider.setFailureModeEvaluator(new DefaultMultifactorAuthenticationFailureModeEvaluator(casProperties));

        val service = RegisteredServiceTestUtils.getRegisteredService();
        servicesManager.save(service);
        WebUtils.putRegisteredService(context, service);

        WebUtils.putMultifactorAuthenticationProviderIdIntoFlowScope(context, provider);
        val event = mfaFailureAction.execute(context);
        assertEquals(transitionId, event.getId());
    }
}
