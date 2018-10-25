package org.apereo.cas.authentication;

import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProviderBypassProperties;
import org.apereo.cas.services.RegisteredService;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GroovyMultifactorAuthenticationProviderBypassTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class GroovyMultifactorAuthenticationProviderBypassTests {
    @Test
    public void verifyAction() {
        assertTrue(runGroovyBypassFor("casuser"));
        assertFalse(runGroovyBypassFor("anotheruser"));
    }

    private static boolean runGroovyBypassFor(final String username) {
        val request = new MockHttpServletRequest();
        val properties = new MultifactorAuthenticationProviderBypassProperties();
        properties.getGroovy().setLocation(new ClassPathResource("GroovyBypass.groovy"));
        val groovy = new GroovyMultifactorAuthenticationProviderBypass(properties);
        val provider = new TestMultifactorAuthenticationProvider();

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
