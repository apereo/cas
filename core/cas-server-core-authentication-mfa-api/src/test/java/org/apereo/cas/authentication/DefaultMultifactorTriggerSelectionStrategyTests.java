package org.apereo.cas.authentication;

import org.apereo.cas.authentication.mfa.MultifactorAuthenticationTestUtils;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultMultifactorTriggerSelectionStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class DefaultMultifactorTriggerSelectionStrategyTests {
    @Test
    public void verifyOperation() {
        val trigger = mock(MultifactorAuthenticationTrigger.class);
        when(trigger.isActivated(any(), any(), any(), any()))
            .thenReturn(Optional.of(new TestMultifactorAuthenticationProvider()));

        val strategy = new DefaultMultifactorTriggerSelectionStrategy(Collections.singletonList(trigger));
        val result = strategy.resolve(new MockHttpServletRequest(), MultifactorAuthenticationTestUtils.getRegisteredService(),
            MultifactorAuthenticationTestUtils.getAuthentication("casuser"),
            MultifactorAuthenticationTestUtils.getService("https://www.example.org"));
        assertNotNull(result);
    }
}
