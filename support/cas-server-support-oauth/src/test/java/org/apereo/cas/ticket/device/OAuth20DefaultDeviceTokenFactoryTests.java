package org.apereo.cas.ticket.device;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.services.DefaultRegisteredServiceOAuthDeviceTokenExpirationPolicy;
import org.apereo.cas.ticket.code.OAuth20Code;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20DefaultDeviceTokenFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("OAuth")
public class OAuth20DefaultDeviceTokenFactoryTests extends AbstractOAuth20Tests {

    @Test
    public void verifyOperationWithExpPolicy() {
        val registeredService = getRegisteredService("https://device.oauth.org", "clientid-device", "secret-at");
        registeredService.setDeviceTokenExpirationPolicy(new DefaultRegisteredServiceOAuthDeviceTokenExpirationPolicy("PT100S"));
        servicesManager.save(registeredService);
        val token = defaultDeviceTokenFactory.createDeviceCode(RegisteredServiceTestUtils.getService("https://device.oauth.org"));
        assertNotNull(token);
        assertNotNull(defaultAccessTokenFactory.get(OAuth20Code.class));
    }
}
