package org.apereo.cas.authentication;

import org.apereo.cas.authentication.mfa.MultifactorAuthenticationTestUtils;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;

import lombok.val;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultMultifactorTriggerSelectionStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class DefaultMultifactorTriggerSelectionStrategyTests {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

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
