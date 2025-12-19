package org.apereo.cas.ticket.device;

import module java.base;
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
@Tag("OAuthToken")
class OAuth20DefaultDeviceUserCodeTests extends AbstractOAuth20Tests {
    @Test
    void verifyOperation() throws Throwable {
        val devCode = defaultDeviceTokenFactory.createDeviceCode(RegisteredServiceTestUtils.getService());
        assertEquals(OAuth20DeviceToken.PREFIX, devCode.getPrefix());

        val uc = defaultDeviceUserCodeFactory.createDeviceUserCode(devCode.getService());
        assertNotNull(uc);
        assertEquals(OAuth20DeviceUserCode.PREFIX, uc.getPrefix());
    }
}
