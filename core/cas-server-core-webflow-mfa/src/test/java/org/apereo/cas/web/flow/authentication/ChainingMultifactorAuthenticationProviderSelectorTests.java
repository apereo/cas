package org.apereo.cas.web.flow.authentication;

import module java.base;
import org.apereo.cas.authentication.DefaultChainingMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationFailureModeEvaluator;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ChainingMultifactorAuthenticationProviderSelectorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("MFA")
class ChainingMultifactorAuthenticationProviderSelectorTests {

    @Test
    void verifyMultipleProviders() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        val evaluator = mock(MultifactorAuthenticationFailureModeEvaluator.class);
        val selector = new ChainingMultifactorAuthenticationProviderSelector(applicationContext, evaluator);

        val provider1 = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val provider2 = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);

        val result = selector.resolve(List.of(provider1, provider2),
            RegisteredServiceTestUtils.getRegisteredService(), RegisteredServiceTestUtils.getPrincipal());
        assertInstanceOf(DefaultChainingMultifactorAuthenticationProvider.class, result);
    }
}
