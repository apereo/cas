package org.apereo.cas.web.flow;

import org.apereo.cas.BaseCasWebflowMultifactorAuthenticationTests;
import org.apereo.cas.authentication.DefaultMultifactorAuthenticationFailureModeEvaluator;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes;
import org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Nested;
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
public class MultifactorAuthenticationFailureActionTests {
    private static class BaseMultifactorActionTests extends BaseCasWebflowMultifactorAuthenticationTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_MFA_CHECK_FAILURE)
        private Action mfaFailureAction;

        protected void executeAction(final MultifactorAuthenticationProviderFailureModes providerMode,
                                     final MultifactorAuthenticationProviderFailureModes serviceMode,
                                     final String transitionId) throws Exception {
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

            val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
            provider.setFailureMode(providerMode);
            provider.setFailureModeEvaluator(new DefaultMultifactorAuthenticationFailureModeEvaluator(casProperties));

            val service = RegisteredServiceTestUtils.getRegisteredService();

            if (serviceMode != null) {
                val policy = new DefaultRegisteredServiceMultifactorPolicy();
                policy.setFailureMode(serviceMode);
                service.setMultifactorAuthenticationPolicy(policy);
            }
            servicesManager.save(service);
            WebUtils.putRegisteredService(context, service);

            WebUtils.putMultifactorAuthenticationProviderIdIntoFlowScope(context, provider);
            val event = mfaFailureAction.execute(context);
            assertEquals(transitionId, event.getId());
        }
    }

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    public class UnavailableModes extends BaseMultifactorActionTests {
        @Test
        public void verifyOperations() throws Exception {
            executeAction(MultifactorAuthenticationProviderFailureModes.CLOSED, null, CasWebflowConstants.TRANSITION_ID_UNAVAILABLE);
            executeAction(MultifactorAuthenticationProviderFailureModes.NONE, null, CasWebflowConstants.TRANSITION_ID_UNAVAILABLE);
            executeAction(MultifactorAuthenticationProviderFailureModes.UNDEFINED, null, CasWebflowConstants.TRANSITION_ID_UNAVAILABLE);
        }
    }

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    public class OpenMode extends BaseMultifactorActionTests {
        @Test
        public void verifyOperations() throws Exception {
            executeAction(MultifactorAuthenticationProviderFailureModes.OPEN, null, CasWebflowConstants.TRANSITION_ID_BYPASS);
        }
    }

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    public class PhantomMode extends BaseMultifactorActionTests {
        @Test
        public void verifyOperations() throws Exception {
            executeAction(MultifactorAuthenticationProviderFailureModes.CLOSED,
                MultifactorAuthenticationProviderFailureModes.PHANTOM, CasWebflowConstants.TRANSITION_ID_BYPASS);
        }
    }

}
