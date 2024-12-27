package org.apereo.cas.authentication.principal;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DelegatedAuthenticationPreProcessorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Delegation")
class DelegatedAuthenticationPreProcessorTests {

    @Test
    void verifyOperation() {
        val policy = mock(DelegatedAuthenticationPreProcessor.class);
        when(policy.getOrder()).thenCallRealMethod();
        assertEquals(Ordered.LOWEST_PRECEDENCE, policy.getOrder());
    }
}
