package org.apereo.cas.metadata.rest;

import module java.base;
import org.apereo.cas.config.CasCoreConfigurationMetadataAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link CasConfigurationMetadataServerEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(
    classes = {
        CasCoreWebAutoConfiguration.class,
        CasCoreConfigurationMetadataAutoConfiguration.class
    },
    properties = {
        "management.endpoint.configurationMetadata.access=UNRESTRICTED",
        "management.endpoints.web.exposure.include=*"
    },
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SpringBootTestAutoConfigurations
@AutoConfigureMockMvc
@ExtendWith(CasTestExtension.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("CasConfiguration")
class CasConfigurationMetadataServerEndpointTests {
    @Autowired
    @Qualifier("mockMvc")
    private MockMvc mockMvc;

    @Test
    void verifyOperation() throws Throwable {
        mockMvc.perform(get("/actuator/configurationMetadata")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isNotEmpty());

        mockMvc.perform(get("/actuator/configurationMetadata/server.port")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isNotEmpty());
    }
}
