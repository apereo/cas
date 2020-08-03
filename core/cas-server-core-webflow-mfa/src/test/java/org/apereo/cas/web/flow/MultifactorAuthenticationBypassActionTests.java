package org.apereo.cas.web.flow;

import org.apereo.cas.BaseCasWebflowMultifactorAuthenticationTests;
import org.apereo.cas.authentication.bypass.MultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.NeverAllowMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
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
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link MultifactorAuthenticationBypassActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Webflow")
public class MultifactorAuthenticationBypassActionTests extends BaseCasWebflowMultifactorAuthenticationTests {
    @Autowired
    @Qualifier("mfaBypassAction")
    private Action mfaBypassAction;

    @Test
    public void verifyOperations() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val service = RegisteredServiceTestUtils.getRegisteredService();
        servicesManager.save(service);
        WebUtils.putRegisteredService(context, service);
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(), context);

        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        provider.setBypassEvaluator(NeverAllowMultifactorAuthenticationProviderBypassEvaluator.getInstance());
        WebUtils.putMultifactorAuthenticationProviderIdIntoFlowScope(context, provider);
        
        val transition = mock(Transition.class);
        when(transition.getId()).thenReturn(CasWebflowConstants.TRANSITION_ID_BYPASS);
        context.setCurrentTransition(transition);
        var event = mfaBypassAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_YES, event.getId());

        when(transition.getId()).thenReturn(CasWebflowConstants.TRANSITION_ID_SUCCESS);
        context.setCurrentTransition(transition);
        event = mfaBypassAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_NO, event.getId());

        val eval = mock(MultifactorAuthenticationProviderBypassEvaluator.class);
        when(eval.shouldMultifactorAuthenticationProviderExecute(any(), any(), any(), any())).thenReturn(Boolean.FALSE);
        provider.setBypassEvaluator(eval);
        event = mfaBypassAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_YES, event.getId());
    }
}
