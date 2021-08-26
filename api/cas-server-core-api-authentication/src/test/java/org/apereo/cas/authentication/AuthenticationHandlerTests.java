package org.apereo.cas.authentication;

import org.apereo.cas.configuration.model.core.authentication.AuthenticationHandlerStates;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("AuthenticationHandler")
public class AuthenticationHandlerTests {

    @Test
    public void verifyOperation() {
        val input = new AuthenticationHandler() {
            @Override
            public AuthenticationHandlerExecutionResult authenticate(final Credential credential) {
                return null;
            }
        };
        assertFalse(input.supports(mock(Credential.class)));
        assertFalse(input.supports(Credential.class));
        assertNotNull(input.getName());
        assertEquals(Integer.MAX_VALUE, input.getOrder());
        assertEquals(AuthenticationHandlerStates.ACTIVE, input.getState());
    }

    @Test
    public void verifyDisabledOperation() {
        val input = AuthenticationHandler.disabled();

        assertEquals(AuthenticationHandlerStates.ACTIVE, input.getState());
        assertFalse(input.supports(mock(Credential.class)));
        assertFalse(input.supports(Credential.class));
        assertNotNull(input.getName());
        assertThrows(PreventedException.class, () -> input.authenticate(mock(Credential.class)));
    }

}
