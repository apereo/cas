package org.apereo.cas.qr.web;

import module java.base;
import org.apereo.cas.qr.BaseQRAuthenticationTokenValidatorServiceTests;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link QRAuthenticationDeviceRepositoryEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("ActuatorEndpoint")
@SpringBootTest(classes = BaseQRAuthenticationTokenValidatorServiceTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.qr.json.location=file:${java.io.tmpdir}/cas-qr-devices.json",
        "management.endpoint.qrDevices.access=UNRESTRICTED",
        "management.endpoints.web.exposure.include=*"
    },
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ExtendWith(CasTestExtension.class)
class QRAuthenticationDeviceRepositoryEndpointTests {
    @Autowired
    @Qualifier("mockMvc")
    private MockMvc mockMvc;

    @Test
    void verifyOperation() throws Throwable {
        val username = "casuser";
        val deviceId = UUID.randomUUID().toString();
        mockMvc.perform(get("/actuator/qrDevices/%s".formatted(username))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());

        mockMvc.perform(post("/actuator/qrDevices/%s/%s".formatted(username, deviceId))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
        
        mockMvc.perform(get("/actuator/qrDevices/%s".formatted(username))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isNotEmpty());

        mockMvc.perform(delete("/actuator/qrDevices/%s".formatted(deviceId))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
    }

}
