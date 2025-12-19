package org.apereo.cas.authentication.policy;

import module java.base;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.PreventedException;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AtLeastOneCredentialValidatedAuthenticationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("AuthenticationPolicy")
class AtLeastOneCredentialValidatedAuthenticationPolicyTests {
    @Test
    void verifyOperationPrevented() throws Throwable {
        val input = new AtLeastOneCredentialValidatedAuthenticationPolicy();
        val builder = new DefaultAuthenticationBuilder(CoreAuthenticationTestUtils.getPrincipal());
        val authn = builder.addFailure("Prevented", new PreventedException("error")).build();
        assertFalse(input.isSatisfiedBy(authn, mock(ConfigurableApplicationContext.class)).isSuccess());
    }

    @Test
    void verifyHandlerCountMismatch() throws Throwable {
        val input = new AtLeastOneCredentialValidatedAuthenticationPolicy(true);
        val builder = new DefaultAuthenticationBuilder(CoreAuthenticationTestUtils.getPrincipal());
        val authn = builder.addFailure("Prevented", new PreventedException("error")).build();
        assertFalse(input.isSatisfiedBy(authn, mock(ConfigurableApplicationContext.class)).isSuccess());
    }

    @Test
    void verifyOperation() throws Throwable {
        val input = new AtLeastOneCredentialValidatedAuthenticationPolicy();
        val builder = new DefaultAuthenticationBuilder(CoreAuthenticationTestUtils.getPrincipal());
        val authn = builder.addSuccess("Handler1", mock(AuthenticationHandlerExecutionResult.class)).build();
        assertTrue(input.isSatisfiedBy(authn, mock(ConfigurableApplicationContext.class)).isSuccess());
    }
}
