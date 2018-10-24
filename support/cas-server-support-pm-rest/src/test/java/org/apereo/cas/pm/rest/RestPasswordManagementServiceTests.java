package org.apereo.cas.pm.rest;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.category.RestfulApiCategory;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.pm.RestPasswordManagementConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pm.PasswordChangeBean;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.config.PasswordManagementConfiguration;
import org.apereo.cas.util.MockWebServer;

import lombok.val;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

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
})
@TestPropertySource(locations = {"classpath:/rest-pm.properties"})
@Category(RestfulApiCategory.class)
public class RestPasswordManagementServiceTests {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    @Qualifier("passwordChangeService")
    private PasswordManagementService passwordChangeService;

    @Autowired
    @Qualifier("passwordManagementCipherExecutor")
    private CipherExecutor passwordManagementCipherExecutor;

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
                props.getAuthn().getPm());

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
                props.getAuthn().getPm());

            val result = passwordService.change(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
                new PasswordChangeBean("123456", "123456"));
            assertTrue(result);
            webServer.stop();
        }
    }
}
