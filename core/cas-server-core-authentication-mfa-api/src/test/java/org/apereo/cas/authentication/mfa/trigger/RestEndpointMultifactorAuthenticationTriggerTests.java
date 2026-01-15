package org.apereo.cas.authentication.mfa.trigger;

import module java.base;
import org.apereo.cas.authentication.DefaultMultifactorAuthenticationProviderResolver;
import org.apereo.cas.authentication.MultifactorAuthenticationPrincipalResolver;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.MockWebServer;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
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
class RestEndpointMultifactorAuthenticationTriggerTests extends BaseSimpleMultifactorAuthenticationTriggerTests {

    @Test
    void verifyNoProviders() {
        try (val webServer = new MockWebServer(TestMultifactorAuthenticationProvider.ID, HttpStatus.OK)) {
            webServer.start();

            val appContext = new StaticApplicationContext();
            appContext.refresh();

            val props = new CasConfigurationProperties();
            props.getAuthn().getMfa().getTriggers().getRest().setUrl("http://localhost:%s".formatted(webServer.getPort()));
            val trigger = new RestEndpointMultifactorAuthenticationTrigger(props,
                new DefaultMultifactorAuthenticationProviderResolver(MultifactorAuthenticationPrincipalResolver.identical()),
                appContext);
            val result = trigger.isActivated(authentication, registeredService, this.httpRequest, this.httpResponse, mock(Service.class));
            assertTrue(result.isEmpty());
        }
    }

    @Test
    void verifyOperationByProvider() {
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
    void verifyFailProvider() {
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
    void verifyNoProvider() {
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

