package org.apereo.cas.authentication;

import org.apereo.cas.authentication.mfa.MultifactorAuthenticationTestUtils;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

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
class DefaultMultifactorAuthenticationTriggerSelectionStrategyTests {
    private static MultifactorAuthenticationTrigger getMultifactorAuthenticationTrigger() throws Throwable {
        val trigger = mock(MultifactorAuthenticationTrigger.class);
        when(trigger.supports(any(), any(), any(), any())).thenReturn(true);
        when(trigger.isActivated(any(), any(), any(), any(), any()))
            .thenReturn(Optional.of(new TestMultifactorAuthenticationProvider()));
        return trigger;
    }

    @Test
    void verifyOperation() throws Throwable {
        val trigger = getMultifactorAuthenticationTrigger();
        val strategy = new DefaultMultifactorAuthenticationTriggerSelectionStrategy(List.of(trigger));
        val result = strategy.resolve(new MockHttpServletRequest(),
            new MockHttpServletResponse(),
            MultifactorAuthenticationTestUtils.getRegisteredService(),
            MultifactorAuthenticationTestUtils.getAuthentication("casuser"),
            MultifactorAuthenticationTestUtils.getService("https://www.example.org"));
        assertTrue(result.isPresent());
    }

    @Test
    void verifyNotSupportingTrigger() throws Throwable {
        val trigger = getMultifactorAuthenticationTrigger();
        when(trigger.supports(any(), any(), any(), any())).thenReturn(false);
        val strategy = new DefaultMultifactorAuthenticationTriggerSelectionStrategy(List.of(trigger));
        assertFalse(strategy.getMultifactorAuthenticationTriggers().isEmpty());
        val registeredService = MultifactorAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getMultifactorAuthenticationPolicy().isBypassEnabled()).thenReturn(true);
        val result = strategy.resolve(new MockHttpServletRequest(), new MockHttpServletResponse(),
            registeredService,
            MultifactorAuthenticationTestUtils.getAuthentication("casuser"),
            MultifactorAuthenticationTestUtils.getService("https://www.example.org"));
        assertTrue(result.isEmpty());
    }

    @Test
    void verifyOperationIgnoringExecution() throws Throwable {
        val trigger = getMultifactorAuthenticationTrigger();
        val strategy = new DefaultMultifactorAuthenticationTriggerSelectionStrategy(List.of(trigger));
        val registeredService = MultifactorAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getMultifactorAuthenticationPolicy().isBypassEnabled()).thenReturn(true);
        val result = strategy.resolve(new MockHttpServletRequest(), new MockHttpServletResponse(),
            registeredService,
            MultifactorAuthenticationTestUtils.getAuthentication("casuser"),
            MultifactorAuthenticationTestUtils.getService("https://www.example.org"));
        assertTrue(result.isEmpty());
    }
}
