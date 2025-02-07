package org.apereo.cas.scim.v2;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.web.flow.BaseScimTests;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ScimPrincipalProvisionerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("SCIM")
@TestPropertySource(properties = {
    "cas.scim.target=http://localhost:9666/scim/v2",
    "cas.scim.username=scim-user",
    "cas.scim.password=changeit",
    "cas.scim.oauth-token=mfh834bsd202usn10snf"
})
@EnabledIfListeningOnPort(port = 9666)
class ScimPrincipalProvisionerTests extends BaseScimTests {
    @Test
    void verifyScimServicePerApp() {
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
        assertNotNull(getPrincipalProvisioningScimService().getScimRequestBuilder(Optional.of(registeredService)));
    }
}
