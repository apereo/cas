package org.apereo.cas.oidc.web.controllers.jwks;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.web.report.AbstractCasEndpointTests;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link OidcJwksEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@TestPropertySource(properties = {
    "CasFeatureModule.OpenIDConnect.client-jwks-registration.enabled=true",
    "cas.authn.oidc.jwks.file-system.jwks-file=file:${#systemProperties['java.io.tmpdir']}/oidc-jwks.jwks",
    "management.endpoint.oidcJwks.access=UNRESTRICTED"
})
@Tag("ActuatorEndpoint")
@Import(AbstractOidcTests.SharedTestConfiguration.class)
@ExtendWith(CasTestExtension.class)
class OidcJwksEndpointTests extends AbstractCasEndpointTests {

    @Test
    void verifyRotation() throws Throwable {
        mockMvc.perform(get("/actuator/oidcJwks/rotate")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.keys").isArray());
    }

    @Test
    void verifyRevocation() throws Throwable {
        mockMvc.perform(get("/actuator/oidcJwks/revoke")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.keys").isArray());
    }

    @Test
    void verifyClientJwksLoadOp() throws Throwable {
        mockMvc.perform(get("/actuator/oidcJwks/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void verifyClientJwksRemoval() throws Throwable {
        mockMvc.perform(delete("/actuator/oidcJwks/clients/%s".formatted(UUID.randomUUID().toString()))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
    }
}
