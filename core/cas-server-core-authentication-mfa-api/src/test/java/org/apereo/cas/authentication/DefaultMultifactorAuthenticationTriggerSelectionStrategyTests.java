package org.apereo.cas.authentication;

import org.apereo.cas.authentication.mfa.MultifactorAuthenticationTestUtils;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultMultifactorAuthenticationTriggerSelectionStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("MFATrigger")
public class DefaultMultifactorAuthenticationTriggerSelectionStrategyTests {
    private static MultifactorAuthenticationTrigger getMultifactorAuthenticationTrigger() {
        val trigger = mock(MultifactorAuthenticationTrigger.class);
        when(trigger.supports(any(), any(), any(), any())).thenReturn(true);
        when(trigger.isActivated(any(), any(), any(), any()))
            .thenReturn(Optional.of(new TestMultifactorAuthenticationProvider()));
        return trigger;
    }

    @Test
    public void verifyOperation() {
        val trigger = getMultifactorAuthenticationTrigger();
        val strategy = new DefaultMultifactorAuthenticationTriggerSelectionStrategy(List.of(trigger));
        val result = strategy.resolve(new MockHttpServletRequest(), MultifactorAuthenticationTestUtils.getRegisteredService(),
            MultifactorAuthenticationTestUtils.getAuthentication("casuser"),
            MultifactorAuthenticationTestUtils.getService("https://www.example.org"));
        assertTrue(result.isPresent());
    }

    @Test
    public void verifyNotSupportingTrigger() {
        val trigger = getMultifactorAuthenticationTrigger();
        when(trigger.supports(any(), any(), any(), any())).thenReturn(false);
        val strategy = new DefaultMultifactorAuthenticationTriggerSelectionStrategy(List.of(trigger));
        assertFalse(strategy.getMultifactorAuthenticationTriggers().isEmpty());
        val registeredService = MultifactorAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getMultifactorPolicy().isBypassEnabled()).thenReturn(true);
        val result = strategy.resolve(new MockHttpServletRequest(), registeredService,
            MultifactorAuthenticationTestUtils.getAuthentication("casuser"),
            MultifactorAuthenticationTestUtils.getService("https://www.example.org"));
        assertTrue(result.isEmpty());
    }

    @Test
    public void verifyOperationIgnoringExecution() {
        val trigger = getMultifactorAuthenticationTrigger();
        val strategy = new DefaultMultifactorAuthenticationTriggerSelectionStrategy(List.of(trigger));
        val registeredService = MultifactorAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getMultifactorPolicy().isBypassEnabled()).thenReturn(true);
        val result = strategy.resolve(new MockHttpServletRequest(), registeredService,
            MultifactorAuthenticationTestUtils.getAuthentication("casuser"),
            MultifactorAuthenticationTestUtils.getService("https://www.example.org"));
        assertTrue(result.isEmpty());
    }
}
