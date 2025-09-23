package org.apereo.cas.pm.rest;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreAuditAutoConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.config.CasPasswordManagementAutoConfiguration;
import org.apereo.cas.config.CasRestPasswordManagementAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.pm.PasswordHistoryService;
import org.apereo.cas.pm.PasswordManagementQuery;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestPasswordManagementServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("RestfulApi")
@ExtendWith(CasTestExtension.class)
class RestPasswordManagementServiceTests {
    @SpringBootTestAutoConfigurations
    @ImportAutoConfiguration({
        CasRestPasswordManagementAutoConfiguration.class,
        CasPasswordManagementAutoConfiguration.class,
        RestTemplateAutoConfiguration.class,
        CasCoreAutoConfiguration.class,
        CasCoreTicketsAutoConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasCoreAuditAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasCoreScriptingAutoConfiguration.class,
        CasCoreCookieAutoConfiguration.class,
        CasCoreWebflowAutoConfiguration.class,
        CasCoreMultifactorAuthenticationAutoConfiguration.class,
        CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class
    })
    @SpringBootConfiguration(proxyBeanMethods = false)
    public static class SharedTestConfiguration {

        @Autowired
        @Qualifier("passwordChangeServiceRestTemplate")
        void setRestTemplate(final RestTemplate restTemplate) {
            restTemplate.getInterceptors().add(new ClientHttpRequestInterceptor() {
                @Override
                public ClientHttpResponse intercept(final HttpRequest request, final byte[] body, final ClientHttpRequestExecution execution) throws IOException {
                    LAST_BODY = body;
                    return execution.execute(request, body);
                }
            });
        }
    }

    private static byte[] LAST_BODY;

    @Nested
    @SpringBootTest(classes = SharedTestConfiguration.class)
    public class UndefinedConfigurationOperations {
        @Autowired
        @Qualifier(PasswordManagementService.DEFAULT_BEAN_NAME)
        private PasswordManagementService passwordChangeService;

        @Test
        void verifyEmailFound() throws Throwable {
            val request = new PasswordChangeRequest("casuser", "current-psw".toCharArray(), "123456".toCharArray(), "123456".toCharArray());
            assertFalse(passwordChangeService.change(request));
            assertNull(passwordChangeService.findEmail(PasswordManagementQuery.builder().username("casuser").build()));
            assertNull(passwordChangeService.findUsername(PasswordManagementQuery.builder().username("casuser").build()));
            assertNull(passwordChangeService.findPhone(PasswordManagementQuery.builder().username("casuser").build()));
            assertNull(passwordChangeService.getSecurityQuestions(PasswordManagementQuery.builder().username("casuser").build()));
            assertTrue(passwordChangeService.unlockAccount(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser")));
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
            "cas.authn.pm.rest.endpoint-url-account-unlock=http://localhost:9092",
            "cas.authn.pm.rest.endpoint-username=username",
            "cas.authn.pm.rest.endpoint-password=password",
            "cas.authn.pm.rest.headers.header1=value1"
        })
    public class BasicOperations {
        @Autowired
        @Qualifier(PasswordManagementService.DEFAULT_BEAN_NAME)
        private PasswordManagementService passwordChangeService;

        @Autowired
        @Qualifier("passwordManagementCipherExecutor")
        private CipherExecutor passwordManagementCipherExecutor;

        @Autowired
        @Qualifier(PasswordHistoryService.BEAN_NAME)
        private PasswordHistoryService passwordHistoryService;

        @Autowired
        @Qualifier("passwordChangeServiceRestTemplate")
        private RestTemplate passwordChangeServiceRestTemplate;

        @Test
        void verifyEmailFound() throws Throwable {
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
        void verifyUserFound() throws Throwable {
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
        void verifyPhoneFound() throws Throwable {
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
        void verifySecurityQuestions() throws Throwable {
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
                val passwordService = getRestPasswordManagementService(props);

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
        void verifyUpdateSecurityQuestions() {
            val query = PasswordManagementQuery.builder().username("casuser").build();
            query.securityQuestion("Q1", "A1");
            try (val webServer = new MockWebServer(9308, HttpStatus.OK)) {
                webServer.start();

                val props = new CasConfigurationProperties();
                val rest = props.getAuthn().getPm().getRest();
                rest.setEndpointUrlChange("http://localhost:9308");
                rest.setEndpointUrlSecurityQuestions("http://localhost:9308");
                rest.setEndpointUrlEmail("http://localhost:9308");
                rest.getHeaders().put("header1", "value1");
                val passwordService = getRestPasswordManagementService(props);

                assertDoesNotThrow(() -> passwordService.updateSecurityQuestions(query));
            }
        }

        private RestPasswordManagementService getRestPasswordManagementService(final CasConfigurationProperties props) {
            return new RestPasswordManagementService(
                passwordManagementCipherExecutor,
                props,
                passwordChangeServiceRestTemplate,
                passwordHistoryService);
        }

        @Test
        void verifyUnlockAccount() {
            try (val webServer = new MockWebServer(9308, HttpStatus.OK)) {
                webServer.start();
                val props = new CasConfigurationProperties();
                val rest = props.getAuthn().getPm().getRest();
                rest.setEndpointUrlAccountUnlock("http://localhost:9308");
                val passwordService = getRestPasswordManagementService(props);
                assertDoesNotThrow(() -> passwordService.unlockAccount(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
            }
        }


        @Test
        void verifyPasswordChanged() throws Throwable {
            val data = "true";
            val request = new PasswordChangeRequest("casuser", "current-psw".toCharArray(), "123456".toCharArray(), "123456".toCharArray());
            try (val webServer = new MockWebServer(9309,
                new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"),
                MediaType.APPLICATION_JSON_VALUE)) {
                webServer.start();

                val props = new CasConfigurationProperties();
                val rest = props.getAuthn().getPm().getRest();
                rest.setEndpointUrlChange("http://localhost:9309");
                rest.setEndpointUrlSecurityQuestions("http://localhost:9309");
                rest.setEndpointUrlEmail("http://localhost:9309");
                val passwordService = getRestPasswordManagementService(props);

                val result = passwordService.change(request);
                assertTrue(result);
                assertThat(LAST_BODY).asString(StandardCharsets.UTF_8).startsWith("{");
                assertThat(LAST_BODY).asString(StandardCharsets.UTF_8).doesNotContain("<", ">");
                webServer.stop();
            }

            try (val webServer = new MockWebServer(9090, HttpStatus.NO_CONTENT)) {
                webServer.start();
                val result = passwordChangeService.change(request);
                assertFalse(result);
                webServer.stop();
            }
        }
    }
}
