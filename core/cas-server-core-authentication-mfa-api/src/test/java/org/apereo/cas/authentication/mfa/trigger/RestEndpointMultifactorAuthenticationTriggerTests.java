package org.apereo.cas.authentication.mfa.trigger;

import org.apereo.cas.authentication.DefaultMultifactorAuthenticationProviderResolver;
import org.apereo.cas.authentication.MultifactorAuthenticationPrincipalResolver;
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
import org.springframework.http.HttpStatus;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RestEndpointMultifactorAuthenticationTriggerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("RestfulApiAuthentication")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RestEndpointMultifactorAuthenticationTriggerTests extends BaseMultifactorAuthenticationTriggerTests {

    @Test
    @Order(0)
    @Tag("DisableProviderRegistration")
    void verifyNoProviders() throws Throwable {
        try (val webServer = new MockWebServer(TestMultifactorAuthenticationProvider.ID, HttpStatus.OK)) {
            webServer.start();
            val props = new CasConfigurationProperties();
            props.getAuthn().getMfa().getTriggers().getRest().setUrl("http://localhost:%s".formatted(webServer.getPort()));
            val trigger = new RestEndpointMultifactorAuthenticationTrigger(props,
                new DefaultMultifactorAuthenticationProviderResolver(MultifactorAuthenticationPrincipalResolver.identical()),
                applicationContext);
            val result = trigger.isActivated(authentication, registeredService, this.httpRequest, this.httpResponse, mock(Service.class));
            assertTrue(result.isEmpty());
        }
    }

    @Test
    @Order(1)
    void verifyOperationByProvider() throws Throwable {
        try (val webServer = new MockWebServer(TestMultifactorAuthenticationProvider.ID, HttpStatus.OK)) {
            webServer.start();
            val props = new CasConfigurationProperties();
            props.getAuthn().getMfa().getTriggers().getRest().setUrl("http://localhost:%s".formatted(webServer.getPort()));
            val trigger = new RestEndpointMultifactorAuthenticationTrigger(props,
                new DefaultMultifactorAuthenticationProviderResolver(MultifactorAuthenticationPrincipalResolver.identical()),
                applicationContext);
            val result = trigger.isActivated(authentication, registeredService, this.httpRequest, this.httpResponse, mock(Service.class));
            assertTrue(result.isPresent());
        }
    }

    @Test
    @Order(2)
    void verifyFailProvider() throws Throwable {
        try (val webServer = new MockWebServer(TestMultifactorAuthenticationProvider.ID, HttpStatus.UNAUTHORIZED)) {
            webServer.start();
            val props = new CasConfigurationProperties();
            props.getAuthn().getMfa().getTriggers().getRest().setUrl("http://localhost:%s".formatted(webServer.getPort()));
            val trigger = new RestEndpointMultifactorAuthenticationTrigger(props,
                new DefaultMultifactorAuthenticationProviderResolver(MultifactorAuthenticationPrincipalResolver.identical()),
                applicationContext);
            val result = trigger.isActivated(authentication, registeredService, this.httpRequest, this.httpResponse, mock(Service.class));
            assertTrue(result.isEmpty());
        }
    }

    @Test
    @Order(3)
    void verifyNoProvider() throws Throwable {
        val props = new CasConfigurationProperties();
        val trigger = new RestEndpointMultifactorAuthenticationTrigger(props,
            new DefaultMultifactorAuthenticationProviderResolver(MultifactorAuthenticationPrincipalResolver.identical()),
            applicationContext);
        var result = trigger.isActivated(authentication, registeredService, this.httpRequest, this.httpResponse, mock(Service.class));
        assertTrue(result.isEmpty());
        props.getAuthn().getMfa().getTriggers().getRest().setUrl("http://localhost:%s".formatted(1234));
        result = trigger.isActivated(null, null, this.httpRequest, this.httpResponse, mock(Service.class));
        assertTrue(result.isEmpty());
    }
}

