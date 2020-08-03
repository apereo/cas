package org.apereo.cas.authentication;

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
@Tag("Authentication")
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
    }

    @Test
    public void verifyDisabledOperation() {
        val input = AuthenticationHandler.disabled();
        
        assertFalse(input.supports(mock(Credential.class)));
        assertFalse(input.supports(Credential.class));
        assertNotNull(input.getName());
        assertThrows(PreventedException.class, () -> input.authenticate(mock(Credential.class)));
    }

}
