package org.apereo.cas.pm.rest;

import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.pm.RestPasswordManagementConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.pm.PasswordHistoryService;
import org.apereo.cas.pm.PasswordManagementQuery;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.config.PasswordManagementConfiguration;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
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
@Tag("RestfulApi")
public class RestPasswordManagementServiceTests {

    @ImportAutoConfiguration({
        MailSenderAutoConfiguration.class,
        AopAutoConfiguration.class,
        RefreshAutoConfiguration.class
    })
    @SpringBootConfiguration
    @Import({
        RestPasswordManagementConfiguration.class,
        PasswordManagementConfiguration.class,
        RestTemplateAutoConfiguration.class,
        CasCoreNotificationsConfiguration.class,
        CasCoreAuditConfiguration.class,
        CasCoreUtilConfiguration.class
    })
    public static class SharedTestConfiguration {
    }

    @Nested
    @SpringBootTest(classes = SharedTestConfiguration.class)
    @SuppressWarnings("ClassCanBeStatic")
    public class UndefinedConfigurationOperations {
        @Autowired
        @Qualifier(PasswordManagementService.DEFAULT_BEAN_NAME)
        private PasswordManagementService passwordChangeService;

        @Test
        public void verifyEmailFound() {
            assertFalse(passwordChangeService.change(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
                new PasswordChangeRequest("casuser", "123456", "123456")));
            assertNull(passwordChangeService.findEmail(PasswordManagementQuery.builder().username("casuser").build()));
            assertNull(passwordChangeService.findUsername(PasswordManagementQuery.builder().username("casuser").build()));
            assertNull(passwordChangeService.findPhone(PasswordManagementQuery.builder().username("casuser").build()));
            assertNull(passwordChangeService.getSecurityQuestions(PasswordManagementQuery.builder().username("casuser").build()));
        }
    }

    @Nested
    @SpringBootTest(classes = SharedTestConfiguration.class,
        properties = {
            "cas.authn.pm.rest.endpoint-url-change=http://localhost:9090",
            "cas.authn.pm.rest.endpoint-url-security-questions=http://localhost:9090",
            "cas.authn.pm.rest.endpoint-url-email=http://localhost:9091",
            "cas.authn.pm.rest.endpoint-url-user=http://localhost:9090",
            "cas.authn.pm.rest.endpoint-url-phone=http://localhost:9092",
            "cas.authn.pm.rest.endpoint-username=username",
            "cas.authn.pm.rest.endpoint-password=password"
        })
    @SuppressWarnings("ClassCanBeStatic")
    public class BasicOperations {
        @Autowired
        @Qualifier(PasswordManagementService.DEFAULT_BEAN_NAME)
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
            try (val webServer = new MockWebServer(9091,
                new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"),
                MediaType.APPLICATION_JSON_VALUE)) {
                webServer.start();
                val email = this.passwordChangeService.findEmail(PasswordManagementQuery.builder().username("casuser").build());
                webServer.stop();
                assertNotNull(email);
                assertEquals(data, email);
            }

            try (val webServer = new MockWebServer(9091, HttpStatus.NO_CONTENT)) {
                webServer.start();
                assertNull(passwordChangeService.findEmail(PasswordManagementQuery.builder().username("casuser").build()));
                webServer.stop();
            }
        }

        @Test
        public void verifyUserFound() {
            val data = "casuser";
            try (val webServer = new MockWebServer(9090,
                new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"),
                MediaType.APPLICATION_JSON_VALUE)) {
                webServer.start();
                val username = this.passwordChangeService.findUsername(PasswordManagementQuery.builder().email("casuser@example.org").build());
                webServer.stop();
                assertNotNull(username);
                assertEquals(data, username);
            }

            try (val webServer = new MockWebServer(9090, HttpStatus.NO_CONTENT)) {
                webServer.start();
                assertNull(passwordChangeService.findUsername(PasswordManagementQuery.builder().username("casuser").build()));
                webServer.stop();
            }
        }

        @Test
        public void verifyPhoneFound() {
            val data = "1234567890";
            try (val webServer = new MockWebServer(9092,
                new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"),
                MediaType.APPLICATION_JSON_VALUE)) {
                webServer.start();
                val ph = this.passwordChangeService.findPhone(PasswordManagementQuery.builder().username("casuser").build());
                webServer.stop();
                assertNotNull(ph);
                assertEquals(data, ph);
            }
            try (val webServer = new MockWebServer(9092, HttpStatus.NO_CONTENT)) {
                webServer.start();
                assertNull(passwordChangeService.findPhone(PasswordManagementQuery.builder().username("casuser").build()));
                webServer.stop();
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

                val questions = passwordService.getSecurityQuestions(PasswordManagementQuery.builder().username("casuser").build());
                assertFalse(questions.isEmpty());
                assertTrue(questions.containsKey("question1"));
                webServer.stop();
            }

            try (val webServer = new MockWebServer(9090, HttpStatus.NO_CONTENT)) {
                webServer.start();
                assertNull(passwordChangeService.getSecurityQuestions(PasswordManagementQuery.builder().username("casuser").build()));
                webServer.stop();
            }
        }


        @Test
        public void verifyUpdateSecurityQuestions() {
            val query = PasswordManagementQuery.builder().username("casuser").build();
            query.securityQuestion("Q1", "A1");
            try (val webServer = new MockWebServer(9308, HttpStatus.OK)) {
                webServer.start();

                val props = new CasConfigurationProperties();
                val rest = props.getAuthn().getPm().getRest();
                rest.setEndpointUrlChange("http://localhost:9308");
                rest.setEndpointUrlSecurityQuestions("http://localhost:9308");
                rest.setEndpointUrlEmail("http://localhost:9308");
                val passwordService = new RestPasswordManagementService(
                    passwordManagementCipherExecutor,
                    props.getServer().getPrefix(),
                    new RestTemplate(),
                    props.getAuthn().getPm(),
                    passwordHistoryService);

                assertDoesNotThrow(new Executable() {
                    @Override
                    public void execute() throws Throwable {
                        passwordService.updateSecurityQuestions(query);
                    }
                });
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

            try (val webServer = new MockWebServer(9090, HttpStatus.NO_CONTENT)) {
                webServer.start();
                val result = passwordChangeService.change(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
                    new PasswordChangeRequest("casuser", "123456", "123456"));
                assertFalse(result);
                webServer.stop();
            }
        }
    }
}
