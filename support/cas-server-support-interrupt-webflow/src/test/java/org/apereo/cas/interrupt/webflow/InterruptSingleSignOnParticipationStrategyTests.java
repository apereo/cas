package org.apereo.cas.interrupt.webflow;

import org.apereo.cas.interrupt.InterruptResponse;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link InterruptSingleSignOnParticipationStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Simple")
public class InterruptSingleSignOnParticipationStrategyTests {
    @Test
    public void verifyStrategyWithoutInterrupt() {
        val s = new InterruptSingleSignOnParticipationStrategy();
        assertFalse(s.isParticipating(new MockRequestContext()));
    }

    @Test
    public void verifyStrategyWithInterruptDisabled() {
        val s = new InterruptSingleSignOnParticipationStrategy();
        val ctx = new MockRequestContext();
        val response = new InterruptResponse();
        response.setSsoEnabled(false);
        InterruptUtils.putInterruptIn(ctx, response);
        assertTrue(s.supports(ctx));
        assertFalse(s.isParticipating(ctx));
    }
}
