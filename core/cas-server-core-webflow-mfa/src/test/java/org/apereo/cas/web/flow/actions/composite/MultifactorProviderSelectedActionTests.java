package org.apereo.cas.web.flow.actions.composite;

import org.apereo.cas.BaseCasWebflowMultifactorAuthenticationTests;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MultifactorProviderSelectedActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowMfaActions")
@TestPropertySource(properties = {
    "cas.authn.mfa.core.provider-selection.provider-selection-enabled=true",
    "cas.authn.mfa.core.provider-selection.provider-selection-optional=true"
})
class MultifactorProviderSelectedActionTests extends BaseCasWebflowMultifactorAuthenticationTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_MULTIFACTOR_PROVIDER_SELECTED)
    private Action action;

    @Autowired
    @Qualifier("multifactorAuthenticationProviderSelectionCookieGenerator")
    private CasCookieBuilder multifactorAuthenticationProviderSelectionCookieGenerator;

    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setParameter(MultifactorProviderSelectedAction.PARAMETER_SELECTED_MFA_PROVIDER, TestMultifactorAuthenticationProvider.ID);
        val result = action.execute(context);
        assertEquals(TestMultifactorAuthenticationProvider.ID, result.getId());
    }

    @Test
    void verifyOperationByRequestContext() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.getFlashScope().put(MultifactorProviderSelectedAction.PARAMETER_SELECTED_MFA_PROVIDER,
            new TestMultifactorAuthenticationProvider());
        val result = action.execute(context);
        assertEquals(TestMultifactorAuthenticationProvider.ID, result.getId());
        context.setRequestCookiesFromResponse();
        val cookie = multifactorAuthenticationProviderSelectionCookieGenerator.retrieveCookieValue(context.getHttpServletRequest());
        assertNotNull(cookie);
        assertEquals(TestMultifactorAuthenticationProvider.ID, cookie);
    }

    @Test
    void verifyOperationOptional() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setParameter(MultifactorProviderSelectedAction.PARAMETER_SELECTED_MFA_PROVIDER, "none");
        val result = action.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SKIP, result.getId());
    }
}
