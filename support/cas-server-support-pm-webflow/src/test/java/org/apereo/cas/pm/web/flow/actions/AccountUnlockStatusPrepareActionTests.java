package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.web.flow.CasWebflowConstants;

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
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AccountUnlockStatusPrepareActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("WebflowAccountActions")
class AccountUnlockStatusPrepareActionTests extends BasePasswordManagementActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_ACCOUNT_UNLOCK_PREPARE)
    protected Action accountUnlockStatusPrepareAction;

    @Test
    void verifyAction() throws Throwable {
        val context = new MockRequestContext();
        context.setCurrentEvent(new Event(this, CasWebflowConstants.TRANSITION_ID_ERROR));
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        val result = accountUnlockStatusPrepareAction.execute(context);
        assertEquals(0, context.getMessageContext().getAllMessages().length);
        assertTrue(context.getConversationScope().contains("captchaValue"));
        assertNull(result);
    }
}
