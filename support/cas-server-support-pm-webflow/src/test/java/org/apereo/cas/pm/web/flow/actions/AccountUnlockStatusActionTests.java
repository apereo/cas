package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AccountUnlockStatusActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("WebflowAccountActions")
class AccountUnlockStatusActionTests extends BasePasswordManagementActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_UNLOCK_ACCOUNT_STATUS)
    protected Action action;

    @Test
    void verifyBadCaptcha() throws Throwable {
        val context = getRequestContext("good", "bad");
        val result = action.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, result.getId());
    }

    @Test
    void verifyAccountUnlock() throws Throwable {
        val context = getRequestContext("good", "good");
        val result = action.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
    }

    private RequestContext getRequestContext(final String captcha, final String givenCaptcha) throws Exception {
        val context = MockRequestContext.create(applicationContext);
        context.setParameter("captchaValue", givenCaptcha);
        val credential = RegisteredServiceTestUtils.getCredentialsWithSameUsernameAndPassword("casuser");
        context.getConversationScope().put(Credential.class.getName(), credential);
        context.getConversationScope().put("captchaValue", captcha);
        return context;
    }
}
