package org.apereo.cas.discovery;

import org.apereo.cas.config.CasDiscoveryProfileAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.web.report.AbstractCasEndpointTests;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link CasServerDiscoveryProfileEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    AbstractCasEndpointTests.SharedTestConfiguration.class,
    CasDiscoveryProfileAutoConfiguration.class
},
    properties = {
        "management.endpoints.web.exposure.include=*",
        "management.endpoint.discoveryProfile.access=UNRESTRICTED"
    })

@Tag("ActuatorEndpoint")
@ExtendWith(CasTestExtension.class)
class CasServerDiscoveryProfileEndpointTests extends AbstractCasEndpointTests {

    @Test
    void verifyOperation() throws Exception {
        mockMvc.perform(get("/actuator/discoveryProfile")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());
    }
}
