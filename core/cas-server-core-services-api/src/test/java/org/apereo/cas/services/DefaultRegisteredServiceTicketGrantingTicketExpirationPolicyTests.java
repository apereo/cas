package org.apereo.cas.services;


import org.apereo.cas.util.MockRequestContext;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultRegisteredServiceTicketGrantingTicketExpirationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("RegisteredService")
class DefaultRegisteredServiceTicketGrantingTicketExpirationPolicyTests {

    @BeforeEach
    void setup() throws Exception {
        MockRequestContext.create()
            .setRemoteAddr("185.86.151.11")
            .setLocalAddr("85.88.06.11")
            .withUserAgent("Firefox")
            .setClientInfo();
    }
    
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

    @Test
    void verifyPolicyByUserAgent() throws Exception {
        val policy = new DefaultRegisteredServiceTicketGrantingTicketExpirationPolicy();
        policy.setUserAgents(Map.of("Fire.+", 10L));
        val expirationPolicy = policy.toExpirationPolicy().orElseThrow();
        assertEquals(10L, expirationPolicy.getTimeToLive());
    }

    @Test
    void verifyPolicyByIpAddress() throws Exception {
        val policy = new DefaultRegisteredServiceTicketGrantingTicketExpirationPolicy();
        policy.setIpAddresses(Map.of(".+86.151.+", 10L));
        val expirationPolicy = policy.toExpirationPolicy().orElseThrow();
        assertEquals(10L, expirationPolicy.getTimeToLive());
    }
}
