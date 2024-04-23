package org.apereo.cas.authentication.policy;

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
 * This is {@link NotPreventedAuthenticationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("AuthenticationPolicy")
class NotPreventedAuthenticationPolicyTests {

    @Test
    void verifyOperationPrevented() throws Throwable {
        val input = new NotPreventedAuthenticationPolicy();
        val builder = new DefaultAuthenticationBuilder(CoreAuthenticationTestUtils.getPrincipal());
        val authn = builder.addFailure("Prevented", new PreventedException("error")).build();
        assertFalse(input.isSatisfiedBy(authn, mock(ConfigurableApplicationContext.class)).isSuccess());
    }

    @Test
    void verifyOperationNotPrevented() throws Throwable {
        val input = new NotPreventedAuthenticationPolicy();
        val authn = new DefaultAuthenticationBuilder(CoreAuthenticationTestUtils.getPrincipal()).build();
        assertFalse(input.isSatisfiedBy(authn, mock(ConfigurableApplicationContext.class)).isSuccess());
    }
}
