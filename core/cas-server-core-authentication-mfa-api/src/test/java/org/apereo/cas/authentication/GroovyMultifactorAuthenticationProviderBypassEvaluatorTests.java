package org.apereo.cas.authentication;

import org.apereo.cas.authentication.bypass.GroovyMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProviderBypassProperties;
import org.apereo.cas.services.RegisteredService;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GroovyMultifactorAuthenticationProviderBypassEvaluatorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Groovy")
public class GroovyMultifactorAuthenticationProviderBypassEvaluatorTests {
    @Test
    public void verifyAction() {
        assertTrue(runGroovyBypassFor("casuser"));
        assertFalse(runGroovyBypassFor("anotheruser"));
    }

    private static boolean runGroovyBypassFor(final String username) {
        val request = new MockHttpServletRequest();
        val properties = new MultifactorAuthenticationProviderBypassProperties();
        properties.getGroovy().setLocation(new ClassPathResource("GroovyBypass.groovy"));
        val provider = new TestMultifactorAuthenticationProvider();
        val groovy = new GroovyMultifactorAuthenticationProviderBypassEvaluator(properties, provider.getId());

        val authentication = mock(Authentication.class);
        val principal = mock(Principal.class);
        when(principal.getId()).thenReturn(username);
        when(authentication.getPrincipal()).thenReturn(principal);
        val registeredService = mock(RegisteredService.class);
        when(registeredService.getName()).thenReturn("Service");
        when(registeredService.getServiceId()).thenReturn("http://app.org");
        when(registeredService.getId()).thenReturn(1000L);
        return groovy.shouldMultifactorAuthenticationProviderExecute(authentication, registeredService, provider, request);
    }
}
