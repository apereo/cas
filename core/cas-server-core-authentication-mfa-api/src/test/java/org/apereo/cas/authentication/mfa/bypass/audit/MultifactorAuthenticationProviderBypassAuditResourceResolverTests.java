package org.apereo.cas.authentication.mfa.bypass.audit;

import org.apereo.cas.authentication.bypass.audit.MultifactorAuthenticationProviderBypassAuditResourceResolver;
import org.apereo.cas.authentication.mfa.MultifactorAuthenticationTestUtils;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;

import lombok.val;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link MultifactorAuthenticationProviderBypassAuditResourceResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("MFATrigger")
public class MultifactorAuthenticationProviderBypassAuditResourceResolverTests {

    @Test
    public void verifyOperation() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        val resolver = new MultifactorAuthenticationProviderBypassAuditResourceResolver();
        val jp = mock(JoinPoint.class);
        val args = new Object[]{
            MultifactorAuthenticationTestUtils.getAuthentication("casuser"),
            MultifactorAuthenticationTestUtils.getRegisteredService(),
            TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext)
        };
        when(jp.getArgs()).thenReturn(args);
        when(jp.getTarget()).thenReturn("TargetObject");
        val outcome = resolver.resolveFrom(jp, new Object());
        assertTrue(outcome.length > 0);
        assertNotNull(resolver.resolveFrom(jp, new RuntimeException("failed")));

        when(jp.getArgs()).thenReturn(null);
        assertEquals(0, resolver.resolveFrom(jp, new Object()).length);
    }

    @Test
    public void verifyJsonOperation() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        val resolver = new MultifactorAuthenticationProviderBypassAuditResourceResolver();
        resolver.setAuditFormat(AuditTrailManager.AuditFormats.JSON);
        
        val jp = mock(JoinPoint.class);
        val args = new Object[]{
            MultifactorAuthenticationTestUtils.getAuthentication("casuser"),
            MultifactorAuthenticationTestUtils.getRegisteredService(),
            TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext)
        };
        when(jp.getArgs()).thenReturn(args);
        when(jp.getTarget()).thenReturn("TargetObject");
        val outcome = resolver.resolveFrom(jp, new Object());
        assertTrue(outcome.length > 0);
        assertNotNull(resolver.resolveFrom(jp, new RuntimeException("failed")));

        when(jp.getArgs()).thenReturn(null);
        assertEquals(0, resolver.resolveFrom(jp, new Object()).length);
    }
}
