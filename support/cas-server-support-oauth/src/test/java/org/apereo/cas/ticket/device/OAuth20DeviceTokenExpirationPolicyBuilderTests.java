package org.apereo.cas.ticket.device;

import org.apereo.cas.AbstractOAuth20Tests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20DeviceTokenExpirationPolicyBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("OAuth")
public class OAuth20DeviceTokenExpirationPolicyBuilderTests extends AbstractOAuth20Tests {
    @Test
    public void verifyOperation() {
        val results = deviceTokenExpirationPolicy.buildTicketExpirationPolicy();
        assertNotNull(results);
        assertEquals(OAuth20DeviceToken.class, deviceTokenExpirationPolicy.getTicketType());
    }
}
