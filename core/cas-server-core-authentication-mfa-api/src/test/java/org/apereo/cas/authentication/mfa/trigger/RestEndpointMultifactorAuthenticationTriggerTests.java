package org.apereo.cas.authentication.mfa.trigger;

import org.apereo.cas.authentication.DefaultMultifactorAuthenticationProviderResolver;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.trigger.RestEndpointMultifactorAuthenticationTrigger;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.MockWebServer;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;

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
public class RestEndpointMultifactorAuthenticationTriggerTests extends BaseMultifactorAuthenticationTriggerTests {

    @Test
    public void verifyOperationByProvider() {
        val response = TestMultifactorAuthenticationProvider.ID.getBytes(StandardCharsets.UTF_8);
        try (val webServer = new MockWebServer(9313,
            new ByteArrayResource(response, "Output"), HttpStatus.OK)) {
            webServer.start();
            val props = new CasConfigurationProperties();
            props.getAuthn().getMfa().setRestEndpoint("http://localhost:9313");
            val trigger = new RestEndpointMultifactorAuthenticationTrigger(props,
                new DefaultMultifactorAuthenticationProviderResolver((providers, service, principal) -> providers.iterator().next()),
                applicationContext);
            val result = trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class));
            assertTrue(result.isPresent());
        }
    }
}

