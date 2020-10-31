package org.apereo.cas.services;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RegisteredServiceAccessStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("RegisteredService")
public class RegisteredServiceAccessStrategyTests {
    @Test
    public void verifyOperation() {
        val component = mock(RegisteredServiceAccessStrategy.class);
        when(component.getDelegatedAuthenticationPolicy()).thenCallRealMethod();
        when(component.getRequiredAttributes()).thenCallRealMethod();
        doCallRealMethod().when(component).setServiceAccessAllowed(anyBoolean());

        assertTrue(component.getRequiredAttributes().isEmpty());
        assertNull(component.getDelegatedAuthenticationPolicy());
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                component.setServiceAccessAllowed(false);
            }
        });
    }
}
