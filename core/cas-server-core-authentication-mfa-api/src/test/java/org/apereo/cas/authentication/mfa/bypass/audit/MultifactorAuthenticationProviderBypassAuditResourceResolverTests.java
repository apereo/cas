package org.apereo.cas.authentication.mfa.bypass.audit;

import org.apereo.cas.authentication.bypass.audit.MultifactorAuthenticationProviderBypassAuditResourceResolver;
import org.apereo.cas.authentication.mfa.MultifactorAuthenticationTestUtils;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;

import lombok.val;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link MultifactorAuthenticationProviderBypassAuditResourceResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = AopAutoConfiguration.class)
public class MultifactorAuthenticationProviderBypassAuditResourceResolverTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @Test
    public void verifyOperation() {
        val resolver = new MultifactorAuthenticationProviderBypassAuditResourceResolver();
        val jp = mock(JoinPoint.class);
        val args = new Object[]{
            MultifactorAuthenticationTestUtils.getAuthentication("casuser"),
            MultifactorAuthenticationTestUtils.getRegisteredService(),
            TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext)
        };
        when(jp.getArgs()).thenReturn(args);
        val outcome = resolver.resolveFrom(jp, new Object());
        assertTrue(outcome.length > 0);
    }
}
