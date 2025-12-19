package org.apereo.cas.authentication;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AuthenticationPreProcessorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Authentication")
class AuthenticationPreProcessorTests {

    @Test
    void verifyOperation() throws Throwable {
        val p = new AuthenticationPreProcessor() {
            @Override
            public boolean process(final AuthenticationTransaction transaction) throws AuthenticationException {
                return false;
            }
        };
        assertEquals(Ordered.HIGHEST_PRECEDENCE, p.getOrder());
        assertTrue(p.supports(mock(Credential.class)));
    }

}
