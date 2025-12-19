package org.apereo.cas.api;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link PasswordlessAuthenticationPreProcessorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Simple")
class PasswordlessAuthenticationPreProcessorTests {
    @Test
    void verifyOperation() {
        val processor = mock(PasswordlessAuthenticationPreProcessor.class);
        when(processor.getOrder()).thenCallRealMethod();
        assertEquals(Ordered.LOWEST_PRECEDENCE, processor.getOrder());
    }
}
