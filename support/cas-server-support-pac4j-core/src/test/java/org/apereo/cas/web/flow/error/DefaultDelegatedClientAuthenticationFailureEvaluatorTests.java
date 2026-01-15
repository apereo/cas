package org.apereo.cas.web.flow.error;

import module java.base;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultDelegatedClientAuthenticationFailureEvaluatorTests}.
 *
 * @author Jerome LELEU
 * @since 7.1.0
 */
@Tag("Delegation")
class DefaultDelegatedClientAuthenticationFailureEvaluatorTests {

    @Test
    void verifyThrottling() {
        val evaluator = new DefaultDelegatedClientAuthenticationFailureEvaluator(
                mock(DelegatedClientAuthenticationConfigurationContext.class));
        val optModelAndView = evaluator.evaluate(new MockHttpServletRequest(), HttpStatus.LOCKED.value());
        assertEquals("error/%s".formatted(HttpStatus.LOCKED.value()), optModelAndView.get().getViewName());
    }
}
