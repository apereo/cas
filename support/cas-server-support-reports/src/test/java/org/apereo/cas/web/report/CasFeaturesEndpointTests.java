package org.apereo.cas.web.report;

import module java.base;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link CasFeaturesEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@TestPropertySource(properties = "management.endpoint.casFeatures.access=UNRESTRICTED")
@Tag("ActuatorEndpoint")
class CasFeaturesEndpointTests extends AbstractCasEndpointTests {
    @Test
    void verifyOperation() throws Exception {
        mockMvc.perform(get("/actuator/casFeatures")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
        assertTrue(CasFeatureModule.FeatureCatalog.Reports.isRegistered());
        assertFalse(CasFeatureModule.FeatureCatalog.Reports.isRegistered("unknown"));
    }
}
