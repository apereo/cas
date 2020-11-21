package org.apereo.cas.uma.discovery;

import org.apereo.cas.uma.web.controllers.BaseUmaEndpointControllerTests;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link UmaServerDiscoverySettingsFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("UMA")
public class UmaServerDiscoverySettingsFactoryTests extends BaseUmaEndpointControllerTests {
    @Test
    public void verifyOperation() {
        assertNotNull(discoverySettings.getAuthorizationEndpoint());
        assertNotNull(discoverySettings.getAuthorizationRequestEndpoint());
        assertNotNull(discoverySettings.getDynamicClientEndpoint());
        assertNotNull(discoverySettings.getIntrospectionEndpoint());
        assertNotNull(discoverySettings.getPermissionRegistrationEndpoint());
        assertNotNull(discoverySettings.getRequestingPartyClaimsEndpoint());
        assertNotNull(discoverySettings.getResourceSetRegistrationEndpoint());
        assertNotNull(discoverySettings.getTokenEndpoint());
    }

}
