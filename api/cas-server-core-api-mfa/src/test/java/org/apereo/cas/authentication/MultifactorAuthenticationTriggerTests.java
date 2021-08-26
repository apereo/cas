package org.apereo.cas.authentication;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link MultifactorAuthenticationTriggerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("MFATrigger")
public class MultifactorAuthenticationTriggerTests {
    @Test
    public void verifyOperation() {
        val input = mock(MultifactorAuthenticationTrigger.class);
        when(input.getOrder()).thenCallRealMethod();
        assertEquals(Ordered.LOWEST_PRECEDENCE, input.getOrder());
    }
}
