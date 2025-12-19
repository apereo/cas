package org.apereo.cas.authentication.bypass;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link NeverAllowMultifactorAuthenticationProviderBypassEvaluatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */

@Tag("MFATrigger")
class NeverAllowMultifactorAuthenticationProviderBypassEvaluatorTests {

    @Test
    void verifyOperation() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val principal = CoreAuthenticationTestUtils.getPrincipal(Map.of("cn", List.of("example")));
        val authentication = CoreAuthenticationTestUtils.getAuthentication(principal);
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        val policy = new DefaultRegisteredServiceMultifactorPolicy();
        when(registeredService.getMultifactorAuthenticationPolicy()).thenReturn(policy);

        val eval = new NeverAllowMultifactorAuthenticationProviderBypassEvaluator(applicationContext);
        assertTrue(eval.shouldMultifactorAuthenticationProviderExecute(authentication, registeredService,
                provider, new MockHttpServletRequest(), CoreAuthenticationTestUtils.getService()));
    }
}
