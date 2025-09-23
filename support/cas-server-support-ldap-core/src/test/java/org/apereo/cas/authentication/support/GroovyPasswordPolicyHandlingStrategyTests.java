package org.apereo.cas.authentication.support;

import org.apereo.cas.authentication.support.password.GroovyPasswordPolicyHandlingStrategy;
import org.apereo.cas.authentication.support.password.PasswordPolicyContext;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ldaptive.auth.AuthenticationResponse;
import org.ldaptive.auth.AuthenticationResultCode;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;

import javax.security.auth.login.AccountExpiredException;

import static org.apereo.cas.util.junit.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GroovyPasswordPolicyHandlingStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Tag("Groovy")
class GroovyPasswordPolicyHandlingStrategyTests {
    @Test
    void verifyStrategySupportsDefault() throws Throwable {
        val resource = new ClassPathResource("lppe-strategy.groovy");
        val strategy = new GroovyPasswordPolicyHandlingStrategy<AuthenticationResponse>(resource, mock(ApplicationContext.class));
        val res = mock(AuthenticationResponse.class);
        when(res.getAuthenticationResultCode()).thenReturn(AuthenticationResultCode.INVALID_CREDENTIAL);
        when(res.isSuccess()).thenReturn(false);

        val results = strategy.handle(res, mock(PasswordPolicyContext.class));
        assertFalse(strategy.supports(null));
        assertTrue(strategy.supports(res));
        assertFalse(results.isEmpty());
    }

    @Test
    void verifyStrategyHandlesErrors() {
        val resource = new ClassPathResource("lppe-strategy-throws-error.groovy");
        val strategy = new GroovyPasswordPolicyHandlingStrategy<AuthenticationResponse>(resource, mock(ApplicationContext.class));
        val res = mock(AuthenticationResponse.class);
        assertThrowsWithRootCause(RuntimeException.class, AccountExpiredException.class, () -> strategy.handle(res, mock(PasswordPolicyContext.class)));
    }
}
