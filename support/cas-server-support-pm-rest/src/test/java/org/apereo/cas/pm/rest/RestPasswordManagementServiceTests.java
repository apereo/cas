package org.apereo.cas.pm.rest;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.pm.RestPasswordManagementConfiguration;
import org.apereo.cas.pm.PasswordChangeBean;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.config.PasswordManagementConfiguration;
import org.apereo.cas.util.MockWebServer;

import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * This is {@link RestPasswordManagementServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    RestPasswordManagementConfiguration.class,
    PasswordManagementConfiguration.class,
    CasCoreUtilConfiguration.class,
    RefreshAutoConfiguration.class
})
@TestPropertySource(locations = {"classpath:/rest-pm.properties"})
public class RestPasswordManagementServiceTests {

    @Autowired
    @Qualifier("passwordChangeService")
    private PasswordManagementService passwordChangeService;

    @Test
    public void verifyEmailFound() {
        val data = "casuser@example.org";
        val webServer = new MockWebServer(9090,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"),
            MediaType.APPLICATION_JSON_VALUE);
        webServer.start();
        val email = this.passwordChangeService.findEmail("casuser");
        webServer.stop();
        assertNotNull(email);
        assertEquals(data, email);
    }

    @Test
    public void verifySecurityQuestions() {
        val data = "{\"question1\":\"answer1\"}";
        val webServer = new MockWebServer(9090,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"),
            MediaType.APPLICATION_JSON_VALUE);
        webServer.start();
        val questions = this.passwordChangeService.getSecurityQuestions("casuser");
        assertFalse(questions.isEmpty());
        assertTrue(questions.containsKey("question1"));
        webServer.stop();
    }

    @Test
    public void verifyPasswordChanged() {
        val data = "true";
        val webServer = new MockWebServer(9090,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"),
            MediaType.APPLICATION_JSON_VALUE);
        webServer.start();
        val result = this.passwordChangeService.change(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
            new PasswordChangeBean("123456", "123456"));
        assertTrue(result);
        webServer.stop();
    }
}
