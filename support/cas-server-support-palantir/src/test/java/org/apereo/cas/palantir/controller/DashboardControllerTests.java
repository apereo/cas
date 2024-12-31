package org.apereo.cas.palantir.controller;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link DashboardControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */

@SpringBootTest(classes = BasePalantirTests.SharedTestConfiguration.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@EnableWebSecurity
@Tag("Web")
@ExtendWith(CasTestExtension.class)
class DashboardControllerTests {
    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void verifyOperation() throws Throwable {
        val model = mvc.perform(get("/palantir/dashboard"))
            .andExpect(status().isOk())
            .andReturn()
            .getModelAndView()
            .getModel();
        assertNotNull(model.get("casServerPrefix"));
        assertTrue(model.containsKey("authentication"));
        assertTrue(model.containsKey("httpRequestMethod"));
        assertTrue(model.containsKey("httpRequestSecure"));
        assertTrue(model.containsKey("actuatorEndpoints"));
        assertTrue(model.containsKey("serviceDefinitions"));
        
        mvc.perform(get("/palantir/")).andExpect(status().isOk());
        mvc.perform(get("/palantir")).andExpect(status().isOk());
    }
}
