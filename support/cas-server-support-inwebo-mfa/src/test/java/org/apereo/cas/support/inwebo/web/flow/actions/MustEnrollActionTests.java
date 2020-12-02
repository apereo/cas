package org.apereo.cas.support.inwebo.web.flow.actions;

import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests {@link MustEnrollAction}.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@Tag("WebflowMfaActions")
public class MustEnrollActionTests extends BaseActionTests {

    private MustEnrollAction action;

    @BeforeEach
    public void setUp() {
        super.setUp();

        action = new MustEnrollAction(mock(MessageSource.class));
    }

    @Test
    public void verifySuccess() {
        val event = action.doExecute(requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
        assertTrue((Boolean) requestContext.getFlowScope().get(WebflowConstants.MUST_ENROLL));
        assertTrue(requestContext.getFlowScope().contains(WebflowConstants.INWEBO_ERROR_MESSAGE));
    }
}
