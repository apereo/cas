package org.apereo.cas.web.flow.authentication;

import org.apereo.cas.authentication.DefaultChainingMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationFailureModeEvaluator;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredServiceTestUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ChainingMultifactorAuthenticationProviderSelectorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("MFA")
public class ChainingMultifactorAuthenticationProviderSelectorTests {

    @Test
    public void verifyMultipleProviders() {
        val evaluator = mock(MultifactorAuthenticationFailureModeEvaluator.class);
        val selector = new ChainingMultifactorAuthenticationProviderSelector(evaluator);

        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        val provider1 = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val provider2 = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);

        val result = selector.resolve(List.of(provider1, provider2),
            RegisteredServiceTestUtils.getRegisteredService(), RegisteredServiceTestUtils.getPrincipal());
        assertTrue(result instanceof DefaultChainingMultifactorAuthenticationProvider);
    }
}
