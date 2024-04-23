package org.apereo.cas.scim.v2;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.model.support.scim.ScimProperties;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ScimV2PrincipalProvisionerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("SCIM")
class ScimV2PrincipalProvisionerTests {
    @Test
    void verifyScimServicePerApp() throws Throwable {
        val provisioner = new ScimV2PrincipalProvisioner(new ScimProperties(),
            new DefaultScimV2PrincipalAttributeMapper());
        assertFalse(provisioner.provision(CoreAuthenticationTestUtils.getPrincipal(),
            CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));

        val props = new LinkedHashMap<String, RegisteredServiceProperty>();
        props.put(RegisteredServiceProperty.RegisteredServiceProperties.SCIM_OAUTH_TOKEN.getPropertyName(),
            new DefaultRegisteredServiceProperty("token"));
        props.put(RegisteredServiceProperty.RegisteredServiceProperties.SCIM_TARGET.getPropertyName(),
            new DefaultRegisteredServiceProperty("https://localhost:9999"));
        props.put(RegisteredServiceProperty.RegisteredServiceProperties.SCIM_USERNAME.getPropertyName(),
            new DefaultRegisteredServiceProperty(Set.of("username")));
        props.put(RegisteredServiceProperty.RegisteredServiceProperties.SCIM_PASSWORD.getPropertyName(),
            new DefaultRegisteredServiceProperty(Set.of("password")));

        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getProperties()).thenReturn(props);
        assertNotNull(provisioner.getScimService(Optional.of(registeredService)));
    }
}
