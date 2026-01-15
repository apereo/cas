package org.apereo.cas.support.inwebo.web.flow.actions;

import module java.base;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests {@link InweboMustEnrollAction}.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@Tag("WebflowMfaActions")
class InweboMustEnrollActionTests extends BaseInweboActionTests {

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_INWEBO_MUST_ENROLL)
    private Action action;
    
    @Test
    void verifySuccess() throws Throwable {
        val event = action.execute(requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
        assertTrue((Boolean) requestContext.getFlowScope().get(InweboWebflowConstants.MUST_ENROLL));
    }
}
