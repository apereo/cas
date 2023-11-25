package org.apereo.cas.support.inwebo.web.flow.actions;

import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests {@link InweboMustEnrollAction}.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@Tag("WebflowMfaActions")
class InweboMustEnrollActionTests extends BaseInweboActionTests {
    
    @Test
    void verifySuccess() throws Throwable {
        val action = new InweboMustEnrollAction();
        val event = action.execute(requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
        assertTrue((Boolean) requestContext.getFlowScope().get(WebflowConstants.MUST_ENROLL));
    }
}
