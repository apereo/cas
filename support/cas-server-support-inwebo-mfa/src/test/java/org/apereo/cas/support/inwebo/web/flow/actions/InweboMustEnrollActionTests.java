package org.apereo.cas.support.inwebo.web.flow.actions;

import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
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
public class InweboMustEnrollActionTests extends BaseActionTests {

    private InweboMustEnrollAction action;

    @BeforeEach
    public void setUp() {
        super.setUp();

        action = new InweboMustEnrollAction();
    }

    @Test
    public void verifySuccess() {
        val event = action.doExecute(requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
        assertTrue((Boolean) requestContext.getFlowScope().get(WebflowConstants.MUST_ENROLL));
    }
}
