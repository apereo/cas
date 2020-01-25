package org.apereo.cas.authentication.support;

import org.apereo.cas.authentication.support.password.GroovyPasswordPolicyHandlingStrategy;
import org.apereo.cas.authentication.support.password.PasswordPolicyContext;
import org.apereo.cas.config.CasCoreUtilConfiguration;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ldaptive.auth.AuthenticationResponse;
import org.ldaptive.auth.AuthenticationResultCode;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;

import javax.security.auth.login.AccountExpiredException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GroovyPasswordPolicyHandlingStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreUtilConfiguration.class
})
@Tag("Groovy")
public class GroovyPasswordPolicyHandlingStrategyTests {
    @Test
    public void verifyStrategySupportsDefault() {
        val resource = new ClassPathResource("lppe-strategy.groovy");

        val s = new GroovyPasswordPolicyHandlingStrategy<AuthenticationResponse>(resource, mock(ApplicationContext.class));
        val res = mock(AuthenticationResponse.class);
        when(res.getAuthenticationResultCode()).thenReturn(AuthenticationResultCode.INVALID_CREDENTIAL);
        when(res.isSuccess()).thenReturn(false);

        val results = s.handle(res, mock(PasswordPolicyContext.class));

        assertFalse(s.supports(null));
        assertTrue(s.supports(res));
        assertFalse(results.isEmpty());
    }

    @Test
    public void verifyStrategyHandlesErrors() {
        val resource = new ClassPathResource("lppe-strategy-throws-error.groovy");
        val s = new GroovyPasswordPolicyHandlingStrategy<AuthenticationResponse>(resource, mock(ApplicationContext.class));
        val res = mock(AuthenticationResponse.class);
        assertThrows(AccountExpiredException.class, () -> s.handle(res, mock(PasswordPolicyContext.class)));
    }
}
