package org.apereo.cas.services;

import org.apereo.cas.ticket.TicketState;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link NeverRegisteredServiceSingleSignOnParticipationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("RegisteredService")
public class NeverRegisteredServiceSingleSignOnParticipationPolicyTests {

    @Test
    public void verifyOperation() {
        val input = new NeverRegisteredServiceSingleSignOnParticipationPolicy();
        assertFalse(input.shouldParticipateInSso(mock(RegisteredService.class), mock(TicketState.class)));
    }

}
