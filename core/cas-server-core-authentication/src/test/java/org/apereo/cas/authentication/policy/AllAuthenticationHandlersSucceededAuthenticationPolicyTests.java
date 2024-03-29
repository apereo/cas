package org.apereo.cas.authentication.policy;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AllAuthenticationHandlersSucceededAuthenticationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("AuthenticationPolicy")
class AllAuthenticationHandlersSucceededAuthenticationPolicyTests {
    @Test
    void verifyOperationPrevented() throws Throwable {
        val input = new AllAuthenticationHandlersSucceededAuthenticationPolicy();
        val builder = new DefaultAuthenticationBuilder(CoreAuthenticationTestUtils.getPrincipal());
        val authn = builder.addFailure("Prevented", new PreventedException("error")).build();
        assertFalse(input.isSatisfiedBy(authn, mock(ConfigurableApplicationContext.class)).isSuccess());
    }

    @Test
    void verifyMismatch() throws Throwable {
        val input = new AllAuthenticationHandlersSucceededAuthenticationPolicy();
        val authn = new DefaultAuthenticationBuilder(CoreAuthenticationTestUtils.getPrincipal()).build();
        assertFalse(input.isSatisfiedBy(authn, Set.of(new SimpleTestUsernamePasswordAuthenticationHandler()),
            mock(ConfigurableApplicationContext.class)).isSuccess());
    }
}
