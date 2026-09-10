package org.apereo.cas.pm.rest;

import module java.base;
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
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.restclient.autoconfigure.RestClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

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
        RestClientAutoConfiguration.class,
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
    }

    @Nested
    @SpringBootTest(classes = SharedTestConfiguration.class, properties = "cas.authn.pm.core.enabled=true")
    public class UndefinedConfigurationOperations {
        @Autowired
        @Qualifier(PasswordManagementService.DEFAULT_BEAN_NAME)
        private PasswordManagementService passwordChangeService;

        @Test
        void verifyEmailFound() throws Throwable {
            val request = new PasswordChangeRequest("casuser", "current-psw".toCharArray(), "123456".toCharArray(), "123456".toCharArray());
            assertFalse(passwordChangeService.change(request));
            assertTrue(passwordChangeService.findEmails(PasswordManagementQuery.builder().username("casuser").build()).isEmpty());
            assertNull(passwordChangeService.findUsername(PasswordManagementQuery.builder().username("casuser").build()));
            assertNull(passwordChangeService.findPhone(PasswordManagementQuery.builder().username("casuser").build()));
            assertTrue(passwordChangeService.getSecurityQuestions(PasswordManagementQuery.builder().username("casuser").build()).isEmpty());
            assertTrue(passwordChangeService.unlockAccount(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser")));
        }
    }

    @Nested
    @SpringBootTest(classes = SharedTestConfiguration.class,
        properties = {
            "cas.authn.pm.core.enabled=true",
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
        @Qualifier("passwordChangeServiceRestClient")
        private RestClient passwordChangeServiceRestClient;

        @Test
        void verifyEmailFound() throws Throwable {
            val data = "casuser@example.org";
            try (val webServer = new MockWebServer(9091,
                new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"),
                MediaType.APPLICATION_JSON_VALUE)) {
                webServer.start();
                val email = this.passwordChangeService.findEmails(PasswordManagementQuery.builder().username("casuser").build());
                webServer.stop();
                assertEquals(Set.of(data), email);
            }

            try (val webServer = new MockWebServer(9091, HttpStatus.NO_CONTENT)) {
                webServer.start();
                assertTrue(passwordChangeService.findEmails(PasswordManagementQuery.builder().username("casuser").build()).isEmpty());
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
            try (val webServer = new MockWebServer(
                new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"),
                MediaType.APPLICATION_JSON_VALUE)) {
                webServer.start();

                val props = new CasConfigurationProperties();
                val rest = props.getAuthn().getPm().getRest();
                rest.setEndpointUrlChange("http://localhost:" + webServer.getPort());
                rest.setEndpointUrlSecurityQuestions("http://localhost:" + webServer.getPort());
                rest.setEndpointUrlEmail("http://localhost:" + webServer.getPort());
                val passwordService = getRestPasswordManagementService(props);

                val questions = passwordService.getSecurityQuestions(PasswordManagementQuery.builder().username("casuser").build());
                assertFalse(questions.isEmpty());
                assertTrue(questions.containsKey("question1"));
                webServer.stop();
            }

            try (val webServer = new MockWebServer(9090, HttpStatus.NO_CONTENT)) {
                webServer.start();
                assertTrue(passwordChangeService.getSecurityQuestions(PasswordManagementQuery.builder().username("casuser").build()).isEmpty());
                webServer.stop();
            }
        }


        @Test
        void verifyUpdateSecurityQuestions() {
            val query = PasswordManagementQuery.builder().username("casuser").build();
            query.securityQuestion("Q1", "A1");
            val builder = passwordChangeServiceRestClient.mutate();
            val server = MockRestServiceServer.bindTo(builder).build();
            val props = new CasConfigurationProperties();
            props.getAuthn().getPm().getRest().setEndpointUrlSecurityQuestions("https://localhost/questions");
            val passwordService = getRestPasswordManagementService(props, builder.build());
            server.expect(requestTo("https://localhost/questions?username=casuser"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Q1", "A1"))
                .andExpect(header("header1", "value1"))
                .andExpect(content().string(StringUtils.EMPTY))
                .andRespond(withSuccess());

            assertDoesNotThrow(() -> passwordService.updateSecurityQuestions(query));
            server.verify();
        }

        private RestPasswordManagementService getRestPasswordManagementService(final CasConfigurationProperties props) {
            return getRestPasswordManagementService(props, passwordChangeServiceRestClient);
        }

        private RestPasswordManagementService getRestPasswordManagementService(final CasConfigurationProperties props, final RestClient restClient) {
            return new RestPasswordManagementService(
                passwordManagementCipherExecutor,
                props,
                restClient,
                passwordHistoryService);
        }

        @Test
        void verifyUnlockAccount() {
            try (val webServer = new MockWebServer(HttpStatus.OK)) {
                webServer.start();
                val props = new CasConfigurationProperties();
                val rest = props.getAuthn().getPm().getRest();
                rest.setEndpointUrlAccountUnlock("http://localhost:" + webServer.getPort());
                val passwordService = getRestPasswordManagementService(props);
                assertDoesNotThrow(() -> passwordService.unlockAccount(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
            }
        }


        @Test
        void verifyPasswordChanged() throws Throwable {
            val data = "true";
            val request = new PasswordChangeRequest("casuser", "current-psw".toCharArray(), "123456".toCharArray(), "123456".toCharArray());
            try (val webServer = new MockWebServer(
                new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"),
                MediaType.APPLICATION_JSON_VALUE)) {
                webServer.start();

                val props = new CasConfigurationProperties();
                val rest = props.getAuthn().getPm().getRest();
                rest.setEndpointUrlChange("http://localhost:" + webServer.getPort());
                rest.setEndpointUrlSecurityQuestions("http://localhost:" + webServer.getPort());
                rest.setEndpointUrlEmail("http://localhost:" + webServer.getPort());
                val restClient = passwordChangeServiceRestClient.mutate()
                    .requestInterceptor((httpRequest, body, execution) -> {
                        assertThat(body).asString(StandardCharsets.UTF_8).startsWith("{");
                        assertThat(body).asString(StandardCharsets.UTF_8).doesNotContain("<", ">");
                        assertEquals(MediaType.APPLICATION_JSON, httpRequest.getHeaders().getContentType());
                        assertEquals("value1", httpRequest.getHeaders().getFirst("header1"));
                        assertEquals("Basic " + HttpHeaders.encodeBasicAuth("username", "password", StandardCharsets.UTF_8),
                            httpRequest.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
                        return execution.execute(httpRequest, body);
                    }).build();
                val passwordService = getRestPasswordManagementService(props, restClient);

                val result = passwordService.change(request);
                assertTrue(result);
                webServer.stop();
            }

            try (val webServer = new MockWebServer(9090, HttpStatus.NO_CONTENT)) {
                webServer.start();
                val result = passwordChangeService.change(request);
                assertFalse(result);
                webServer.stop();
            }
        }

        @Test
        void verifyHttpErrors() {
            val builder = passwordChangeServiceRestClient.mutate();
            val server = MockRestServiceServer.bindTo(builder).build();
            val props = new CasConfigurationProperties();
            props.getAuthn().getPm().getRest().setEndpointUrlChange("https://localhost/password");
            val passwordService = getRestPasswordManagementService(props, builder.build());
            val request = new PasswordChangeRequest("casuser", "current-psw".toCharArray(), "123456".toCharArray(), "123456".toCharArray());

            for (val status : List.of(HttpStatus.BAD_REQUEST, HttpStatus.INTERNAL_SERVER_ERROR)) {
                server.expect(requestTo("https://localhost/password"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withStatus(status));
                val exception = assertThrows(RestClientResponseException.class, () -> passwordService.changeInternal(request));
                assertEquals(status, exception.getStatusCode());
                server.verify();
                server.reset();
            }
        }

        @Test
        void verifyConfiguredEndpointEncoding() {
            val builder = passwordChangeServiceRestClient.mutate();
            val server = MockRestServiceServer.bindTo(builder).build();
            val props = new CasConfigurationProperties();
            props.getAuthn().getPm().getRest().setEndpointUrlChange("https://localhost/password change");
            val passwordService = getRestPasswordManagementService(props, builder.build());
            server.expect(requestTo("https://localhost/password%20change"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("""
                    {"username":"casuser", "password":"123456", "oldPassword":"current-psw"}
                    """))
                .andRespond(withSuccess("true", MediaType.APPLICATION_JSON));

            val request = new PasswordChangeRequest("casuser", "current-psw".toCharArray(), "123456".toCharArray(), "123456".toCharArray());
            assertTrue(passwordService.changeInternal(request));
            server.verify();
        }
    }
}
