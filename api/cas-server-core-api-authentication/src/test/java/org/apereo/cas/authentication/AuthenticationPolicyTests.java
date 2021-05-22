package org.apereo.cas.authentication;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AuthenticationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("AuthenticationPolicy")
public class AuthenticationPolicyTests {

    @Test
    public void verifyOperation() {
        val policy = mock(AuthenticationPolicy.class);
        when(policy.getOrder()).thenCallRealMethod();
        when(policy.shouldResumeOnFailure(any())).thenCallRealMethod();
        when(policy.getName()).thenCallRealMethod();
        assertNotNull(policy.getName());
        assertTrue(policy.shouldResumeOnFailure(new IllegalArgumentException()));
        assertEquals(Ordered.LOWEST_PRECEDENCE, policy.getOrder());
    }
}
