package org.apereo.cas.pm.rest;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.pm.RestPasswordManagementConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.pm.PasswordHistoryService;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.config.PasswordManagementConfiguration;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestPasswordManagementServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    RestPasswordManagementConfiguration.class,
    PasswordManagementConfiguration.class,
    RestTemplateAutoConfiguration.class,
    CasCoreUtilConfiguration.class,
    RefreshAutoConfiguration.class
},
    properties = {
        "cas.authn.pm.rest.endpointUrlChange=http://localhost:9090",
        "cas.authn.pm.rest.endpointUrlSecurityQuestions=http://localhost:9090",
        "cas.authn.pm.rest.endpointUrlEmail=http://localhost:9090",
        "cas.authn.pm.rest.endpointUrlPhone=http://localhost:9090"
    })
@Tag("RestfulApi")
public class RestPasswordManagementServiceTests {
    @Autowired
    @Qualifier("passwordChangeService")
    private PasswordManagementService passwordChangeService;

    @Autowired
    @Qualifier("passwordManagementCipherExecutor")
    private CipherExecutor passwordManagementCipherExecutor;

    @Autowired
    @Qualifier("passwordHistoryService")
    private PasswordHistoryService passwordHistoryService;

    @Test
    public void verifyEmailFound() {
        val data = "casuser@example.org";
        try (val webServer = new MockWebServer(9090,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"),
            MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            val email = this.passwordChangeService.findEmail("casuser");
            webServer.stop();
            assertNotNull(email);
            assertEquals(data, email);
        }
    }

    @Test
    public void verifyPhoneFound() {
        val data = "1234567890";
        try (val webServer = new MockWebServer(9090,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"),
            MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            val ph = this.passwordChangeService.findPhone("casuser");
            webServer.stop();
            assertNotNull(ph);
            assertEquals(data, ph);
        }
    }

    @Test
    public void verifySecurityQuestions() {
        val data = "{\"question1\":\"answer1\"}";
        try (val webServer = new MockWebServer(9308,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"),
            MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();

            val props = new CasConfigurationProperties();
            val rest = props.getAuthn().getPm().getRest();
            rest.setEndpointUrlChange("http://localhost:9308");
            rest.setEndpointUrlSecurityQuestions("http://localhost:9308");
            rest.setEndpointUrlEmail("http://localhost:9308");
            val passwordService = new RestPasswordManagementService(passwordManagementCipherExecutor,
                props.getServer().getPrefix(),
                new RestTemplate(),
                props.getAuthn().getPm(),
                passwordHistoryService);

            val questions = passwordService.getSecurityQuestions("casuser");
            assertFalse(questions.isEmpty());
            assertTrue(questions.containsKey("question1"));
            webServer.stop();
        }
    }

    @Test
    public void verifyPasswordChanged() {
        val data = "true";
        try (val webServer = new MockWebServer(9309,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"),
            MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();

            val props = new CasConfigurationProperties();
            val rest = props.getAuthn().getPm().getRest();
            rest.setEndpointUrlChange("http://localhost:9309");
            rest.setEndpointUrlSecurityQuestions("http://localhost:9309");
            rest.setEndpointUrlEmail("http://localhost:9309");
            val passwordService = new RestPasswordManagementService(passwordManagementCipherExecutor,
                props.getServer().getPrefix(),
                new RestTemplate(),
                props.getAuthn().getPm(),
                passwordHistoryService);

            val result = passwordService.change(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
                new PasswordChangeRequest("casuser", "123456", "123456"));
            assertTrue(result);
            webServer.stop();
        }
    }
}
