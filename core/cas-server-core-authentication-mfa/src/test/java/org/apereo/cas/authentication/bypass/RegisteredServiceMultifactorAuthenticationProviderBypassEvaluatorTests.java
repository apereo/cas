package org.apereo.cas.authentication.bypass;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RegisteredServiceMultifactorAuthenticationProviderBypassEvaluatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("MFA")
public class RegisteredServiceMultifactorAuthenticationProviderBypassEvaluatorTests {

    @Test
    public void verifyOperation() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        
        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);

        val eval = new DefaultChainingMultifactorAuthenticationBypassProvider();
        eval.addMultifactorAuthenticationProviderBypassEvaluator(
            new RegisteredServiceMultifactorAuthenticationProviderBypassEvaluator(TestMultifactorAuthenticationProvider.ID));

        val principal = CoreAuthenticationTestUtils.getPrincipal(Map.of("cn", List.of("example")));
        val authentication = CoreAuthenticationTestUtils.getAuthentication(principal);
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        val policy = new DefaultRegisteredServiceMultifactorPolicy();
        policy.setBypassEnabled(true);
        when(registeredService.getMultifactorPolicy()).thenReturn(policy);
        assertFalse(eval.shouldMultifactorAuthenticationProviderExecute(authentication, registeredService, provider, new MockHttpServletRequest()));
    }
}
