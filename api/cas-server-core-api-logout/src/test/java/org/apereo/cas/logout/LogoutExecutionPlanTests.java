package org.apereo.cas.logout;

import org.apereo.cas.logout.slo.SingleLogoutServiceMessageHandler;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link LogoutExecutionPlanTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Logout")
class LogoutExecutionPlanTests {
    @Test
    void verifyOperation() throws Throwable {
        val plan = new LogoutExecutionPlan() {
        };
        assertTrue(plan.getLogoutPostProcessors().isEmpty());
        assertTrue(plan.getSingleLogoutServiceMessageHandlers().isEmpty());
        assertDoesNotThrow(() -> {
            plan.registerSingleLogoutServiceMessageHandler(mock(SingleLogoutServiceMessageHandler.class));
            plan.registerLogoutPostProcessor(mock(LogoutPostProcessor.class));
        });

    }

}
