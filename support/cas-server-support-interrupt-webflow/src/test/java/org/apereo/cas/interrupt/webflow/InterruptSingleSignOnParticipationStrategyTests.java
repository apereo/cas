package org.apereo.cas.interrupt.webflow;

import org.apereo.cas.interrupt.InterruptResponse;
import org.apereo.cas.services.ServicesManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link InterruptSingleSignOnParticipationStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(SpringRunner.class)
public class InterruptSingleSignOnParticipationStrategyTests {

    @Test
    public void verifyStrategyWithoutInterrupt() {
        final InterruptSingleSignOnParticipationStrategy s =
            new InterruptSingleSignOnParticipationStrategy(mock(ServicesManager.class), true);
        assertTrue(s.isParticipating(new MockRequestContext()));
    }

    @Test
    public void verifyStrategyWithInterruptDisabled() {
        final InterruptSingleSignOnParticipationStrategy s =
            new InterruptSingleSignOnParticipationStrategy(mock(ServicesManager.class), true);
        final MockRequestContext ctx = new MockRequestContext();
        final InterruptResponse response = new InterruptResponse();
        response.setSsoEnabled(false);
        InterruptUtils.putInterruptIn(ctx, response);
        assertFalse(s.isParticipating(ctx));
    }
}
