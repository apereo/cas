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
public class DefaultRegisteredServiceTicketGrantingTicketExpirationPolicyTests {

    @Test
    public void verifyOperation() {
        val policy = new DefaultRegisteredServiceTicketGrantingTicketExpirationPolicy();
        policy.setMaxTimeToLiveInSeconds(10);
        assertFalse(policy.toExpirationPolicy().isEmpty());
    }

    @Test
    public void verifyNoPolicy() {
        val policy = new DefaultRegisteredServiceTicketGrantingTicketExpirationPolicy();
        assertTrue(policy.toExpirationPolicy().isEmpty());
    }
}
