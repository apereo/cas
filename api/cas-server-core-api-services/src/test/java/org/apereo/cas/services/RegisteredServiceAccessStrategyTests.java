package org.apereo.cas.services;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RegisteredServiceAccessStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("RegisteredService")
class RegisteredServiceAccessStrategyTests {
    @Test
    void verifyOperation() {
        val component = mock(RegisteredServiceAccessStrategy.class);
        when(component.getDelegatedAuthenticationPolicy()).thenCallRealMethod();
        when(component.getRequiredAttributes()).thenCallRealMethod();
        assertTrue(component.getRequiredAttributes().isEmpty());
        assertNull(component.getDelegatedAuthenticationPolicy());
    }
}
