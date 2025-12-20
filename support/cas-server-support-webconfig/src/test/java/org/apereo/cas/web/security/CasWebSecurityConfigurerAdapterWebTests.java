package org.apereo.cas.web.security;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link CasWebSecurityConfigurerAdapterWebTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(
    classes = {
        CasWebSecurityConfigurerAdapterWebTests.WebTestConfiguration.class,
        BaseWebSecurityTests.SharedTestConfiguration.class
    },
    properties = {
        "spring.security.user.name=casuser",
        "spring.security.user.password=Mellon",
        "spring.web.resources.static-locations=file:${java.io.tmpdir}/static",

        "management.endpoints.access.default=UNRESTRICTED",
        "management.endpoints.web.exposure.include=*",

        "cas.monitor.endpoints.endpoint.beans.access=ANONYMOUS",
        "cas.monitor.endpoints.endpoint.info.access=DENY",
        "cas.monitor.endpoints.endpoint.env.access=AUTHENTICATED",
        "cas.monitor.endpoints.endpoint.conditions.access=PERMIT",

        "cas.monitor.endpoints.endpoint.health.access=IP_ADDRESS",
        "cas.monitor.endpoints.endpoint.health.required-ip-addresses[0]=196.+",
        "cas.monitor.endpoints.endpoint.health.required-ip-addresses[1]=10.0.0.0/24",
        "cas.monitor.endpoints.endpoint.health.required-ip-addresses[2]=172.16.0.0/16",
        "cas.monitor.endpoints.endpoint.health.required-ip-addresses[3]=200\\\\.0\\\\.0\\\\....",
        "cas.monitor.endpoints.form-login-enabled=true"
    }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("ApacheTomcat")
@ExtendWith(CasTestExtension.class)
@Slf4j
class CasWebSecurityConfigurerAdapterWebTests {
    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mvc;

    @BeforeAll
    public static void beforeAll() throws Exception {
        val parent = new File(System.getProperty("java.io.tmpdir"), "static");
        if (!parent.exists()) {
            assertTrue(parent.mkdirs());
        }
        val root = new File(parent, "hello");
        if (!root.exists()) {
            assertTrue(root.mkdirs());
        }
        val data = new File(root, "data.txt");
        FileUtils.writeStringToFile(data, "Hello, World!", StandardCharsets.UTF_8);
        LOGGER.info("Writing static data file to [{}]", data);
    }

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
    void verifyCasLoginBasicAuth() throws Throwable {
        mvc.perform(post("/cas/login")
                .with(httpBasic("clientid", "clientsecret")))
            .andExpect(status().isOk());
    }

    @Test
    void verifyAccessToEndpoints() throws Throwable {
        mvc.perform(get("/cas/actuator/beans")).andExpect(status().isOk());
        mvc.perform(get("/cas/actuator/conditions")).andExpect(status().isOk());

        mvc.perform(get("/cas/actuator/info")
                .with(httpBasic("casuser", "Mellon")))
            .andExpect(status().isForbidden());
        mvc.perform(get("/cas/actuator/info"))
            .andExpect(status().isUnauthorized());
        mvc.perform(get("/cas/actuator/env")
                .with(httpBasic("casuser", "Mellon")))
            .andExpect(status().isOk());
        mvc.perform(get("/cas/actuator/health")
                .header("X-Forwarded-For", "196.1.1.0"))
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
        mvc.perform(get("/cas/custom")).andExpect(status().isUnauthorized());
    }

    @Test
    void verifyStaticResources() throws Throwable {
        mvc.perform(get("/cas/hello/data.txt")).andExpect(status().isOk());
    }

    @TestConfiguration(value = "WebTestConfiguration", proxyBeanMethods = false)
    static class WebTestConfiguration {

        static class BaseController {
            @GetMapping
            public ResponseEntity getMethod() {
                return ResponseEntity.ok().build();
            }

            @PostMapping
            public ResponseEntity postMethod() {
                return ResponseEntity.ok().build();
            }
        }

        @RestController("AccessTokenController")
        @RequestMapping("/oidc/accessToken")
        static class AccessTokenController extends BaseController {
        }

        @RestController("LoginController")
        @RequestMapping("/login")
        static class LoginController extends BaseController {
        }

        @RestController("CustomController")
        @RequestMapping("/custom")
        static class CustomController extends BaseController {
        }
    }
}
