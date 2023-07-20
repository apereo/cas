package org.apereo.cas.services;


import org.apereo.cas.ticket.AuthenticationAwareTicket;

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
class NeverRegisteredServiceSingleSignOnParticipationPolicyTests {

    @Test
    void verifyOperation() {
        val input = new NeverRegisteredServiceSingleSignOnParticipationPolicy();
        assertFalse(input.shouldParticipateInSso(mock(RegisteredService.class), mock(AuthenticationAwareTicket.class)));
    }

}
