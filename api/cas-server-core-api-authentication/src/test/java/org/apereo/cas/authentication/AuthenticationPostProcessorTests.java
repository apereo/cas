package org.apereo.cas.authentication;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AuthenticationPostProcessorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Authentication")
class AuthenticationPostProcessorTests {

    @Test
    void verifyOperation() throws Throwable {
        assertEquals(Ordered.HIGHEST_PRECEDENCE, AuthenticationPostProcessor.none().getOrder());
        assertTrue(AuthenticationPostProcessor.none().supports(mock(Credential.class)));
        assertDoesNotThrow(AuthenticationPostProcessor.none()::destroy);
    }
}
