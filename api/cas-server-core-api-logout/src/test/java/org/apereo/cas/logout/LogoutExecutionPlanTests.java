package org.apereo.cas.logout;

import org.apereo.cas.logout.slo.SingleLogoutServiceMessageHandler;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link LogoutExecutionPlanTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Logout")
public class LogoutExecutionPlanTests {
    @Test
    public void verifyOperation() {
        val plan = new LogoutExecutionPlan() {
        };
        assertTrue(plan.getLogoutPostProcessors().isEmpty());
        assertTrue(plan.getSingleLogoutServiceMessageHandlers().isEmpty());
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() {
                plan.registerSingleLogoutServiceMessageHandler(mock(SingleLogoutServiceMessageHandler.class));
                plan.registerLogoutPostProcessor(mock(LogoutPostProcessor.class));
            }
        });

    }

}
