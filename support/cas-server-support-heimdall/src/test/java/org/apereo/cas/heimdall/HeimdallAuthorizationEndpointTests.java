package org.apereo.cas.heimdall;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link HeimdallAuthorizationEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Tag("Authorization")
@ExtendWith(CasTestExtension.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = BaseHeimdallTests.SharedTestConfiguration.class,
    properties = {
        "cas.monitor.endpoints.endpoint.defaults.access=ANONYMOUS",
        "management.endpoints.web.exposure.include=*",
        "management.endpoint.heimdall.access=UNRESTRICTED",
        "cas.authn.oidc.jwks.file-system.jwks-file=file:${#systemProperties['java.io.tmpdir']}/heimdalloidc.jwks",
        "cas.heimdall.json.location=classpath:/policies"
    }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class HeimdallAuthorizationEndpointTests {
    @Autowired
    @Qualifier("mockMvc")
    private MockMvc mockMvc;

    @Test
    void verifyOperation() throws Exception {
        val authzRequest = AuthorizationRequest.builder()
            .uri("/api/claims")
            .method("PUT")
            .namespace("API_CLAIMS")
            .build()
            .toJson();
        mockMvc.perform(post("/actuator/heimdall/resource")
                .contentType(MediaType.APPLICATION_JSON)
                .content(authzRequest))
            .andExpect(status().isOk());
    }
    
    @Test
    void verifyAllResources() throws Exception {
        mockMvc.perform(get("/actuator/heimdall/resources")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        
        mockMvc.perform(get("/actuator/heimdall/resources/API_CLAIMS")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        mockMvc.perform(get("/actuator/heimdall/resources/API_CLAIMS/1453626")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void verifyNotFound() throws Exception {
        val authzRequest = AuthorizationRequest.builder()
            .uri("/api/claims")
            .method("OPTIONS")
            .namespace("API_CLAIMS")
            .build()
            .toJson();
        mockMvc.perform(post("/actuator/heimdall/resource")
                .contentType(MediaType.APPLICATION_JSON)
                .content(authzRequest))
            .andExpect(status().isNotFound());
    }
}
