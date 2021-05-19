package org.apereo.cas.interrupt.webflow;

import org.apereo.cas.interrupt.InterruptResponse;
import org.apereo.cas.web.flow.SingleSignOnParticipationRequest;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
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
        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .httpServletRequest(new MockHttpServletRequest())
            .requestContext(new MockRequestContext())
            .build();
        assertFalse(s.isParticipating(ssoRequest));
    }

    @Test
    public void verifyStrategyWithInterruptDisabled() {
        val strategy = new InterruptSingleSignOnParticipationStrategy();
        val ctx = new MockRequestContext();
        val response = new InterruptResponse();
        response.setSsoEnabled(false);
        InterruptUtils.putInterruptIn(ctx, response);

        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .httpServletRequest(new MockHttpServletRequest())
            .requestContext(ctx)
            .build();

        assertTrue(strategy.supports(ssoRequest));
        assertFalse(strategy.isParticipating(ssoRequest));
    }
}
