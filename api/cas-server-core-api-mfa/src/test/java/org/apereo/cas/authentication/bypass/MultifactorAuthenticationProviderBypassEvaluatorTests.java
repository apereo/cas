package org.apereo.cas.authentication.bypass;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.core.Ordered;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link MultifactorAuthenticationProviderBypassEvaluatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("MFATrigger")
public class MultifactorAuthenticationProviderBypassEvaluatorTests {
    @Test
    public void verifyOperation() {
        val input = mock(MultifactorAuthenticationProviderBypassEvaluator.class);
        when(input.isMultifactorAuthenticationBypassed(any(), anyString())).thenCallRealMethod();
        when(input.getOrder()).thenCallRealMethod();
        when(input.size()).thenCallRealMethod();
        doCallRealMethod().when(input).forgetBypass(any());
        doCallRealMethod().when(input).rememberBypass(any(), any());

        assertEquals(1, input.size());
        assertEquals(Ordered.LOWEST_PRECEDENCE, input.getOrder());
        assertFalse(input.isMultifactorAuthenticationBypassed(mock(Authentication.class), "provider-id"));
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                input.rememberBypass(mock(Authentication.class), mock(MultifactorAuthenticationProvider.class));
                input.forgetBypass(mock(Authentication.class));
            }
        });
    }
}
