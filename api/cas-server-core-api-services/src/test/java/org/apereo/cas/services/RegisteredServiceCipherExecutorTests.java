package org.apereo.cas.services;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RegisteredServiceCipherExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("RegisteredService")
public class RegisteredServiceCipherExecutorTests {
    @Test
    public void verifyOperation() {
        val component = mock(RegisteredServiceCipherExecutor.class);
        when(component.isEnabled()).thenCallRealMethod();
        when(component.supports(any())).thenCallRealMethod();

        assertTrue(component.isEnabled());
        assertTrue(component.supports(mock(RegisteredService.class)));
        assertNotNull(RegisteredServiceCipherExecutor.noOp().decode("data", Optional.empty()));
    }
}
