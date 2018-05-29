package org.apereo.cas.authentication;

import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProviderBypassProperties;
import org.apereo.cas.services.RegisteredService;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.Assert.*;
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

    private boolean runGroovyBypassFor(final String username) {
        final var request = new MockHttpServletRequest();
        final var properties = new MultifactorAuthenticationProviderBypassProperties();
        properties.getGroovy().setLocation(new ClassPathResource("GroovyBypass.groovy"));
        final var groovy = new GroovyMultifactorAuthenticationProviderBypass(properties);
        final var provider = new TestMultifactorAuthenticationProvider();

        final var authentication = mock(Authentication.class);
        final var principal = mock(Principal.class);
        when(principal.getId()).thenReturn(username);
        when(authentication.getPrincipal()).thenReturn(principal);
        final var registeredService = mock(RegisteredService.class);
        when(registeredService.getName()).thenReturn("Service");
        when(registeredService.getServiceId()).thenReturn("http://app.org");
        when(registeredService.getId()).thenReturn(1000L);
        return groovy.shouldMultifactorAuthenticationProviderExecute(authentication, registeredService, provider, request);
    }
}
