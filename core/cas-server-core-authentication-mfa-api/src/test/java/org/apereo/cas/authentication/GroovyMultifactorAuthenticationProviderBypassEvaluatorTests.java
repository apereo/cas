package org.apereo.cas.authentication;

import org.apereo.cas.authentication.bypass.GroovyMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProviderBypassProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.UUID;

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
        assertTrue(runGroovyBypassFor(getAuthentication("casuser")));
        assertFalse(runGroovyBypassFor(getAuthentication("anotheruser")));
        assertTrue(runGroovyBypassFor(mock(Authentication.class)));
    }

    private static boolean runGroovyBypassFor(final Authentication authentication) {
        val request = new MockHttpServletRequest();
        val properties = new MultifactorAuthenticationProviderBypassProperties();
        properties.getGroovy().setLocation(new ClassPathResource("GroovyBypass.groovy"));
        val provider = new TestMultifactorAuthenticationProvider();
        val groovy = new GroovyMultifactorAuthenticationProviderBypassEvaluator(properties, provider.getId());

        val registeredService = mock(RegisteredService.class);
        when(registeredService.getName()).thenReturn("Service");
        when(registeredService.getServiceId()).thenReturn("http://app.org");
        when(registeredService.getId()).thenReturn(1000L);

        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        ApplicationContextProvider.holdApplicationContext(applicationContext);
        ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext,
            MultifactorAuthenticationPrincipalResolver.identical(), UUID.randomUUID().toString());
        
        return groovy.shouldMultifactorAuthenticationProviderExecute(authentication, registeredService, provider, request);
    }

    private static Authentication getAuthentication(final String username) {
        val authentication = mock(Authentication.class);
        val principal = mock(Principal.class);
        when(principal.getId()).thenReturn(username);
        when(authentication.getPrincipal()).thenReturn(principal);
        return authentication;
    }
}
