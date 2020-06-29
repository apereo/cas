package org.apereo.cas.authentication.mfa.trigger;

import org.apereo.cas.authentication.DefaultMultifactorAuthenticationProviderResolver;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.MockWebServer;

import lombok.val;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RestEndpointMultifactorAuthenticationTriggerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("RestfulApi")
@DirtiesContext
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RestEndpointMultifactorAuthenticationTriggerTests extends BaseMultifactorAuthenticationTriggerTests {

    @Test
    @Order(0)
    @Tag("DisableProviderRegistration")
    public void verifyNoProviders() {
        val response = TestMultifactorAuthenticationProvider.ID.getBytes(StandardCharsets.UTF_8);
        try (val webServer = new MockWebServer(9313,
            new ByteArrayResource(response, "Output"), HttpStatus.OK)) {
            webServer.start();
            val props = new CasConfigurationProperties();
            props.getAuthn().getMfa().getRest().setUrl("http://localhost:9313");
            val trigger = new RestEndpointMultifactorAuthenticationTrigger(props,
                new DefaultMultifactorAuthenticationProviderResolver(),
                applicationContext);
            val result = trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class));
            assertTrue(result.isEmpty());
        }
    }

    @Test
    @Order(1)
    public void verifyOperationByProvider() {
        val response = TestMultifactorAuthenticationProvider.ID.getBytes(StandardCharsets.UTF_8);
        try (val webServer = new MockWebServer(9313,
            new ByteArrayResource(response, "Output"), HttpStatus.OK)) {
            webServer.start();
            val props = new CasConfigurationProperties();
            props.getAuthn().getMfa().getRest().setUrl("http://localhost:9313");
            val trigger = new RestEndpointMultifactorAuthenticationTrigger(props,
                new DefaultMultifactorAuthenticationProviderResolver(),
                applicationContext);
            val result = trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class));
            assertTrue(result.isPresent());
        }
    }

    @Test
    @Order(2)
    public void verifyFailProvider() {
        val response = TestMultifactorAuthenticationProvider.ID.getBytes(StandardCharsets.UTF_8);
        try (val webServer = new MockWebServer(9313,
            new ByteArrayResource(response, "Output"), HttpStatus.UNAUTHORIZED)) {
            webServer.start();
            val props = new CasConfigurationProperties();
            props.getAuthn().getMfa().getRest().setUrl("http://localhost:9313");
            val trigger = new RestEndpointMultifactorAuthenticationTrigger(props,
                new DefaultMultifactorAuthenticationProviderResolver(),
                applicationContext);
            val result = trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class));
            assertTrue(result.isEmpty());
        }
    }

    @Test
    @Order(3)
    public void verifyNoProvider() {
        val props = new CasConfigurationProperties();
        val trigger = new RestEndpointMultifactorAuthenticationTrigger(props,
            new DefaultMultifactorAuthenticationProviderResolver(),
            applicationContext);
        var result = trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class));
        assertTrue(result.isEmpty());
        props.getAuthn().getMfa().getRest().setUrl("http://localhost:9313");
        result = trigger.isActivated(null, null, this.httpRequest, mock(Service.class));
        assertTrue(result.isEmpty());
    }
}

