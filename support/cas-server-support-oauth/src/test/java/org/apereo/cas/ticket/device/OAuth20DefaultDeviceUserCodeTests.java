package org.apereo.cas.ticket.device;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.services.RegisteredServiceTestUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20DefaultDeviceUserCodeTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("OAuth")
public class OAuth20DefaultDeviceUserCodeTests extends AbstractOAuth20Tests {
    @Test
    public void verifyOperation() {
        val devCode = defaultDeviceTokenFactory.createDeviceCode(RegisteredServiceTestUtils.getService());
        assertEquals(OAuth20DeviceToken.PREFIX, devCode.getPrefix());

        val uc = defaultDeviceUserCodeFactory.createDeviceUserCode(devCode);
        assertNotNull(uc);
        assertEquals(OAuth20DeviceUserCode.PREFIX, uc.getPrefix());
    }
}
