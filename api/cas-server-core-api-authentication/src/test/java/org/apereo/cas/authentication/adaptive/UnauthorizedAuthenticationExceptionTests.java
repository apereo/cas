package org.apereo.cas.authentication.adaptive;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link UnauthorizedAuthenticationExceptionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Authentication")
public class UnauthorizedAuthenticationExceptionTests {
    @Test
    public void verifyOpsErrorsAndMessage() {
        val ex = new UnauthorizedAuthenticationException("message", Map.of("error", new RuntimeException()));
        assertNotNull(ex.getMessage());
        assertFalse(ex.getHandlerErrors().isEmpty());
    }

    @Test
    public void verifyOpsErrors() {
        val ex = new UnauthorizedAuthenticationException(Map.of("error", new RuntimeException()));
        assertFalse(ex.getHandlerErrors().isEmpty());
    }

    @Test
    public void verifyOpsErrorsAndResult() {
        val ex = new UnauthorizedAuthenticationException(Map.of("error", new RuntimeException()),
            Map.of("result", mock(AuthenticationHandlerExecutionResult.class)));
        assertFalse(ex.getHandlerErrors().isEmpty());
        assertFalse(ex.getHandlerSuccesses().isEmpty());
    }

    @Test
    public void verifyOpsErrorsAndResultMsg() {
        val ex = new UnauthorizedAuthenticationException("message",
            Map.of("error", new RuntimeException()),
            Map.of("result", mock(AuthenticationHandlerExecutionResult.class)));
        assertFalse(ex.getHandlerErrors().isEmpty());
        assertFalse(ex.getHandlerSuccesses().isEmpty());
        assertNotNull(ex.getMessage());
    }
}
