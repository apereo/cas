package org.apereo.cas.pm.rest;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.pm.RestPasswordManagementConfiguration;
import org.apereo.cas.pm.PasswordChangeBean;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.config.PasswordManagementConfiguration;
import org.apereo.cas.util.MockWebServer;
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
import java.util.Map;

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
        final String data = "casuser@example.org";
        final MockWebServer webServer = new MockWebServer(9090,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"),
            MediaType.APPLICATION_JSON_VALUE);
        webServer.start();
        final String email = this.passwordChangeService.findEmail("casuser");
        webServer.stop();
        assertNotNull(email);
        assertEquals(data, email);
    }

    @Test
    public void verifySecurityQuestions() {
        final String data = "{\"question1\":\"answer1\"}";
        final MockWebServer webServer = new MockWebServer(9090,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"),
            MediaType.APPLICATION_JSON_VALUE);
        webServer.start();
        final Map questions = this.passwordChangeService.getSecurityQuestions("casuser");
        assertFalse(questions.isEmpty());
        assertTrue(questions.containsKey("question1"));
        webServer.stop();
    }

    @Test
    public void verifyPasswordChanged() {
        final String data = "true";
        final MockWebServer webServer = new MockWebServer(9090,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"),
            MediaType.APPLICATION_JSON_VALUE);
        webServer.start();
        final boolean result = this.passwordChangeService.change(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
            new PasswordChangeBean("123456", "123456"));
        assertTrue(result);
        webServer.stop();
    }
}
