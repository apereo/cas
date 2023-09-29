package org.apereo.cas.palantir.controller;

import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPalantirConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.util.serialization.StringSerializer;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link ServicesControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SpringBootTest(
    classes = {
        RefreshAutoConfiguration.class,
        WebMvcAutoConfiguration.class,
        ServletWebServerFactoryAutoConfiguration.class,
        CasRegisteredServicesTestConfiguration.class,
        CasCoreServicesConfiguration.class,
        CasCoreWebConfiguration.class,
        CasCoreNotificationsConfiguration.class,
        CasPalantirConfiguration.class
    },
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@EnableWebSecurity
@Tag("Web")
public class ServicesControllerTests {
    @Autowired
    private ConfigurableWebApplicationContext webApplicationContext;

    private MockMvc mvc;

    private StringSerializer<RegisteredService> registeredServiceSerializer;

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        registeredServiceSerializer = new RegisteredServiceJsonSerializer(webApplicationContext);
    }

    @Test
    void verifyOperation() throws Throwable {
        var mvcResult = mvc.perform(get("/palantir/services")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn();
        val services = registeredServiceSerializer.fromList(mvcResult.getResponse().getContentAsString());
        assertFalse(services.isEmpty());

        mvc.perform(get("/palantir/services/101010")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isNotFound());

        mvcResult = mvc.perform(get("/palantir/services/" + services.getFirst().getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn();
        var registeredService = registeredServiceSerializer.from(mvcResult.getResponse().getContentAsString());
        assertNotNull(registeredService);

        mvcResult = mvc.perform(delete("/palantir/services/" + services.getFirst().getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn();
        registeredService = registeredServiceSerializer.from(mvcResult.getResponse().getContentAsString());
        assertNotNull(registeredService);

        val newService = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        val json = registeredServiceSerializer.toString(newService);
        mvc.perform(post("/palantir/services")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));

        mvc.perform(put("/palantir/services")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }
}
