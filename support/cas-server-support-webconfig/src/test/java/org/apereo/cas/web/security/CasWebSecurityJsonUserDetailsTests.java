package org.apereo.cas.web.security;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import java.util.List;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link CasWebSecurityJsonUserDetailsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@SpringBootTest(classes = BaseWebSecurityTests.SharedTestConfiguration.class, properties = {
    "management.endpoints.access.default=UNRESTRICTED",
    "management.endpoints.web.exposure.include=*",

    "cas.monitor.endpoints.endpoint.defaults.access=ROLE",
    "cas.monitor.endpoints.endpoint.defaults.requiredRoles=ADMIN",

    "cas.monitor.endpoints.json.location=classpath:/StaticUserDetails.json"
}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("WebApp")
@ExtendWith(CasTestExtension.class)
@Slf4j
class CasWebSecurityJsonUserDetailsTests {
    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .apply(springSecurity())
            .defaultRequest(get("/")
                .contextPath("/cas")
                .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN)
                .contentType(MediaType.APPLICATION_JSON))
            .build();
    }

    @Test
    void verifyAccessToEndpoints() throws Throwable {
        val endpoints = List.of("beans", "conditions", "info", "env", "health");
        for (val endpoint : endpoints) {
            mvc.perform(get("/cas/actuator/" + endpoint)
                    .with(httpBasic("casadmin", "pa$$w0rd")))
                .andExpect(status().isOk());
        }
        for (val endpoint : endpoints) {
            mvc.perform(get("/cas/actuator/" + endpoint)
                    .with(httpBasic("casuser", "pa$$w0rd")))
                .andExpect(status().isForbidden());
        }
    }

}
