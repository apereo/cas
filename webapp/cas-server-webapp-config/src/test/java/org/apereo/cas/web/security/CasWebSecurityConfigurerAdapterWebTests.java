package org.apereo.cas.web.security;

import org.apereo.cas.configuration.CasConfigurationProperties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link CasWebSecurityConfigurerAdapterWebTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = {
    CasWebSecurityConfigurerAdapterWebTests.WebTestConfiguration.class,
    BaseWebSecurityTests.SharedTestConfiguration.class
},
    properties = {
        "server.port=8080",

        "spring.security.user.name=casuser",
        "spring.security.user.password=Mellon",

        "management.endpoints.enabled-by-default=true",
        "management.endpoints.web.exposure.include=*",

        "cas.monitor.endpoints.endpoint.beans.access=ANONYMOUS",
        "cas.monitor.endpoints.endpoint.info.access=DENY",
        "cas.monitor.endpoints.endpoint.env.access=AUTHENTICATED",

        "cas.monitor.endpoints.endpoint.health.access=IP_ADDRESS",
        "cas.monitor.endpoints.endpoint.health.required-ip-addresses[0]=196.+",
        "cas.monitor.endpoints.endpoint.health.required-ip-addresses[1]=10.0.0.0/24",
        "cas.monitor.endpoints.endpoint.health.required-ip-addresses[2]=172.16.0.0/16",
        "cas.monitor.endpoints.endpoint.health.required-ip-addresses[3]=200\\\\.0\\\\.0\\\\....",

        "cas.monitor.endpoints.form-login-enabled=true"
    }, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("WebApp")
public class CasWebSecurityConfigurerAdapterWebTests {
    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mvc;

    @BeforeEach
    public void setup() {
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
    public void verifyAccessToEndpoints() throws Exception {
        mvc.perform(get("/cas/actuator/beans")).andExpect(status().isOk());
        mvc.perform(get("/cas/actuator/info")
                .with(httpBasic("casuser", "Mellon")))
            .andExpect(status().isForbidden());
        mvc.perform(get("/cas/actuator/info"))
            .andExpect(status().isUnauthorized());
        mvc.perform(get("/cas/actuator/env")
                .with(httpBasic("casuser", "Mellon")))
            .andExpect(status().isOk());
        mvc.perform(get("/cas/actuator/health").header("X-Forwarded-For", "196.1.1.0"))
            .andExpect(status().isOk());
        mvc.perform(get("/cas/actuator/health").header("X-Forwarded-For", "10.0.0.9"))
                .andExpect(status().isOk());
        mvc.perform(get("/cas/actuator/health").header("X-Forwarded-For", "172.16.55.9"))
                .andExpect(status().isOk());
        mvc.perform(get("/cas/actuator/health").header("X-Forwarded-For", "200.0.0.123"))
                .andExpect(status().isOk());
        mvc.perform(get("/cas/actuator/health").header("X-Forwarded-For", "192.168.0.1"))
                .andExpect(status().isUnauthorized());

        mvc.perform(get("/cas/actuator/health")).andExpect(status().isUnauthorized());
    }

    @TestConfiguration(value = "WebTestConfiguration", proxyBeanMethods = false)
    public static class WebTestConfiguration {

        @RestController("TestController")
        @RequestMapping("/oidc/accessToken")
        @SuppressWarnings("ClassCanBeStatic")
        public class TestController {
            @GetMapping
            public ResponseEntity getMethod() {
                return ResponseEntity.ok().build();
            }

            @PostMapping
            public ResponseEntity postMethod() {
                return ResponseEntity.ok().build();
            }
        }
    }
}
