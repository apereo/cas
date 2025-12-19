package org.apereo.cas.web.flow.actions;

import module java.base;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link StaticEventExecutionActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowActions")
class StaticEventExecutionActionTests {
    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create();
        val action = new StaticEventExecutionAction("custom");
        assertEquals("custom", action.execute(context).getId());

        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS,
            StaticEventExecutionAction.SUCCESS.execute(context).getId());
        assertNull(StaticEventExecutionAction.NULL.execute(context));
    }

}
