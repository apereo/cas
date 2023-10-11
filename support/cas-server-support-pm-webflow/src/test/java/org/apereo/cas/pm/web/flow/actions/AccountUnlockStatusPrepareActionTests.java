package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
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
        val context = MockRequestContext.create(applicationContext).withDefaultMessageContext();
        context.setCurrentEvent(new Event(this, CasWebflowConstants.TRANSITION_ID_ERROR));
        val result = accountUnlockStatusPrepareAction.execute(context);
        assertEquals(0, context.getMessageContext().getAllMessages().length);
        assertTrue(context.getConversationScope().contains("captchaValue"));
        assertNull(result);
    }
}
