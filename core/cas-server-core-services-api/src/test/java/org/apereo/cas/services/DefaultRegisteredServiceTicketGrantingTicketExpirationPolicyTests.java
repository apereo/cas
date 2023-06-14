package org.apereo.cas.services;


import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultRegisteredServiceTicketGrantingTicketExpirationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("RegisteredService")
class DefaultRegisteredServiceTicketGrantingTicketExpirationPolicyTests {

    @Test
    void verifyOperation() {
        val policy = new DefaultRegisteredServiceTicketGrantingTicketExpirationPolicy();
        policy.setMaxTimeToLiveInSeconds(10);
        assertFalse(policy.toExpirationPolicy().isEmpty());
    }

    @Test
    void verifyNoPolicy() {
        val policy = new DefaultRegisteredServiceTicketGrantingTicketExpirationPolicy();
        assertTrue(policy.toExpirationPolicy().isEmpty());
    }
}
