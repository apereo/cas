package org.apereo.cas.web.report;

import module java.base;
import org.apereo.cas.util.http.HttpUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link CasDefaultEndpointSecurityTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@TestPropertySource(properties = {
    "management.endpoints.access.default=UNRESTRICTED",
    "cas.monitor.endpoints.endpoint.defaults.access=AUTHENTICATED",
    "spring.security.user.name=casuser",
    "spring.security.user.password=Mellon"
})
@Tag("ActuatorEndpoint")
class CasDefaultEndpointSecurityTests extends AbstractCasEndpointTests {
    @Test
    void verifyOperation() throws Exception {
        mockMvc.perform(get("/actuator/casFeatures")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .headers(HttpUtils.createBasicAuthHeaders("casuser", "Mellon"))
        ).andExpect(status().isOk());
    }
}
